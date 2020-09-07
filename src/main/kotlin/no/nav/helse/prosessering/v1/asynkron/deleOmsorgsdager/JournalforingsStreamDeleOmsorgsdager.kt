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
import java.net.URI

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
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI4YjZiY2EyOS1kYjk4LTQ0MDMtOGJhMC1lOWY0MDBiZDlmNzIifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI4YjZiY2EyOS1kYjk4LTQ0MDMtOGJhMC1lOWY0MDBiZDlmNzIifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJhZDk3YjJlYy05ZDViLTQ0ZjgtODc5YS03YjFmOGViN2VjMzAifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJhZDk3YjJlYy05ZDViLTQ0ZjgtODc5YS03YjFmOGViN2VjMzAifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI2YzZlYjc3OS1hYWIyLTQ3YTMtOGVjZC1jYWZiYzNjNjg1ZDgifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI2YzZlYjc3OS1hYWIyLTQ3YTMtOGVjZC1jYWZiYzNjNjg1ZDgifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI3Y2QzNzcwOS0xMGNjLTQyODYtOTM4ZC1jYmRlMGU4M2E2MTAifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI3Y2QzNzcwOS0xMGNjLTQyODYtOTM4ZC1jYmRlMGU4M2E2MTAifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJiNjY2NGMyZi03M2U1LTRhNjQtYjdlZi1iNjJkZDQyMDQ5YjkifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJiNjY2NGMyZi03M2U1LTRhNjQtYjdlZi1iNjJkZDQyMDQ5YjkifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJiY2RlYmY0Zi02ZGNlLTRlNjktOGNhYS1kZThiYmIzZGE5M2QifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJiY2RlYmY0Zi02ZGNlLTRlNjktOGNhYS1kZThiYmIzZGE5M2QifQ"))
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJiY2RlYmY0Zi02ZGNlLTRlNjktOGNhYS1kZThiYmIzZGE5M2QifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI5MzRjMjNmZi1mMzU0LTRiYmEtOTJjZS1lMWRmMjQ0ZDM0NTgifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI5MzRjMjNmZi1mMzU0LTRiYmEtOTJjZS1lMWRmMjQ0ZDM0NTgifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJjNWNjNWIwZS1mYjBlLTRjZTMtYjMzOC0zYjRlNmVlYzMwNTUifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiJjNWNjNWIwZS1mYjBlLTRjZTMtYjMzOC0zYjRlNmVlYzMwNTUifQ"))
                }
                .filter{_, entry -> val flatMap = entry.deserialiserTilPreprossesertDeleOmsorgsdagerV1().dokumentUrls.flatMap { it }
                    logger.info("Ignorerer søknad med url https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI4ZWQyZmQ3NS0yYTE2LTQ3M2UtYjQ2Zi05MzA5NzNmOGE2YzYifQ")
                    !flatMap.contains(URI("https://k9-dokument.nais.preprod.local/v1/dokument/eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoibm9uZSJ9.eyJqdGkiOiI4ZWQyZmQ3NS0yYTE2LTQ3M2UtYjQ2Zi05MzA5NzNmOGE2YzYifQ"))
                }
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
