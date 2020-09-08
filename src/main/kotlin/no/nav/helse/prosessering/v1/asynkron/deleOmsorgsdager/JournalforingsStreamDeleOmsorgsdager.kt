package no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager

import no.nav.helse.CorrelationId
import no.nav.helse.aktoer.AktørId
import no.nav.helse.joark.JoarkGateway
import no.nav.helse.kafka.KafkaConfig
import no.nav.helse.kafka.ManagedKafkaStreams
import no.nav.helse.kafka.ManagedStreamHealthy
import no.nav.helse.kafka.ManagedStreamReady
import no.nav.helse.prosessering.v1.asynkron.*
import no.nav.helse.prosessering.v1.deleOmsorgsdager.PreprossesertDeleOmsorgsdagerV1
import no.nav.helse.prosessering.v1.overforeDager.Fosterbarn
import no.nav.helse.prosessering.v1.overforeDager.PreprossesertSøker
import no.nav.k9.søknad.felles.Barn
import no.nav.k9.søknad.felles.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.Søker
import no.nav.k9.søknad.felles.SøknadId
import no.nav.k9.søknad.omsorgspenger.overføring.Mottaker
import no.nav.k9.søknad.omsorgspenger.overføring.OmsorgspengerOverføringSøknad
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory

internal class JournalforingsStreamDeleOmsorgsdager(
    joarkGateway: JoarkGateway,
    kafkaConfig: KafkaConfig
) {

    private val stream = ManagedKafkaStreams(
        name = NAME,
        properties = kafkaConfig.stream(NAME),
        topology = topology(joarkGateway),
        unreadyAfterStreamStoppedIn = kafkaConfig.unreadyAfterStreamStoppedIn
    )

    internal val ready = ManagedStreamReady(stream)
    internal val healthy = ManagedStreamHealthy(stream)

    private companion object {
        private const val NAME = "JournalforingV1DeleOmsorgsdager"
        private val logger = LoggerFactory.getLogger("no.nav.$NAME.topology")

        private fun topology(joarkGateway: JoarkGateway): Topology {
            val builder = StreamsBuilder()
            val fraPreprossesertV1 = Topics.PREPROSSESERT_DELE_OMSORGSDAGER
            val tilCleanup = Topics.CLEANUP_DELE_OMSORGSDAGER

            val mapValues = builder
                .stream(fraPreprossesertV1.name, fraPreprossesertV1.consumed)
                .filter { _, entry -> 1 == entry.metadata.version }
                .mapValues { soknadId, entry ->
                    process(NAME, soknadId, entry) {
                        val preprosessertMelding =
                            entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1()

                        val dokumenter = preprosessertMelding.dokumentUrls
                        logger.info("Journalfører deling av omsorgsdager dokumenter: {}", dokumenter)
                        val journaPostId = joarkGateway.journalførOverforeDager(
                            mottatt = preprosessertMelding.mottatt,
                            aktørId = AktørId(preprosessertMelding.søker.aktørId),
                            norskIdent = preprosessertMelding.søker.fødselsnummer,
                            correlationId = CorrelationId(entry.metadata.correlationId),
                            dokumenter = dokumenter
                        )
                        logger.info("Dokumenter til deling av  omsorgsdager journalført med ID = ${journaPostId.journalpostId}.")
                        //TODO: Lage tilK9.....


                        val journalfort = JournalfortDeleOmsorgsdager(
                            journalpostId = journaPostId.journalpostId,
                                søknad = preprosessertMelding.tilK9DeleOmsorgsdager()
                            )

                        CleanupDeleOmsorgsdager(
                            metadata = entry.metadata,
                            meldingV1 = preprosessertMelding,
                            journalførtMelding = journalfort
                        ).serialiserTilData()
                    }
                }
            mapValues
                .to(tilCleanup.name, tilCleanup.produced)
            return builder.build()
        }
    }

    internal fun stop() = stream.stop(becauseOfError = false)
}

private fun PreprossesertDeleOmsorgsdagerV1.tilK9DeleOmsorgsdager(): OmsorgspengerOverføringSøknad { //TODO: Denne må lages spesifikt for denne typen melding. Dette er bare en kopi av søknad
    val builder = OmsorgspengerOverføringSøknad.builder()
        .søknadId(SøknadId.of(soknadId))
        .mottattDato(mottatt)
        .mottaker("12345678911".tilK9Mottaker()) //TODO: Fjernes, lagt på for test
        .søker(søker.tilK9Søker())

    return builder.build()
}

private fun List<Fosterbarn>.tilK9Barn(): List<Barn> {
    return map {
        Barn.builder()
            .norskIdentitetsnummer(NorskIdentitetsnummer.of(it.fødselsnummer))
            .build()
    }
}

private fun PreprossesertSøker.tilK9Søker() = Søker.builder()
    .norskIdentitetsnummer(NorskIdentitetsnummer.of(fødselsnummer))
    .build()

private fun String.tilK9Mottaker() = Mottaker.builder()
    .norskIdentitetsnummer(NorskIdentitetsnummer.of(this))
    .build()
