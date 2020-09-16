package no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager

import de.huxhorn.sulky.ulid.ULID
import no.nav.helse.CorrelationId
import no.nav.helse.aktoer.AktørId
import no.nav.helse.dokument.DokumentService
import no.nav.helse.kafka.KafkaConfig
import no.nav.helse.kafka.ManagedKafkaStreams
import no.nav.helse.kafka.ManagedStreamHealthy
import no.nav.helse.kafka.ManagedStreamReady
import no.nav.helse.prosessering.v1.asynkron.*
import no.nav.helse.prosessering.v1.deleOmsorgsdager.BarnOgAndreBarn
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.LoggerFactory


internal class CleanupStreamDeleOmsorgsdager(
    kafkaConfig: KafkaConfig,
    dokumentService: DokumentService
) {
    private val stream = ManagedKafkaStreams(
        name = NAME,
        properties = kafkaConfig.stream(NAME),
        topology = topology(dokumentService),
        unreadyAfterStreamStoppedIn = kafkaConfig.unreadyAfterStreamStoppedIn
    )

    internal val ready = ManagedStreamReady(stream)
    internal val healthy = ManagedStreamHealthy(stream)

    private companion object {
        private const val NAME = "CleanupV1DeleOmsorgsdager"
        private val logger = LoggerFactory.getLogger("no.nav.$NAME.topology")

        private fun topology(dokumentService: DokumentService): Topology {
            val builder = StreamsBuilder()
            val fraCleanup = Topics.CLEANUP_DELE_OMSORGSDAGER
            val tilJournalfort= Topics.JOURNALFORT_DELE_OMSORGSDAGER //TODO: Lage egen topic som går til K9-Sak - K9_RAPID_V1

            builder
                .stream(fraCleanup.name, fraCleanup.consumed)
                .filter { _, entry -> 1 == entry.metadata.version }
                .mapValues { soknadId, entry ->
                    process(NAME, soknadId, entry) {
                        val cleanupMelding = entry.deserialiserTilCleanupDeleOmsorgsdager()
                        logger.info("Sletter dele omsorgsdager dokumenter.")
                        dokumentService.slettDokumeter(
                            urlBolks = cleanupMelding.meldingV1.dokumentUrls,
                            aktørId = AktørId(cleanupMelding.meldingV1.søker.aktørId),
                            correlationId = CorrelationId(entry.metadata.correlationId)
                        )
                        logger.info("Dokumenter slettet.")

                        //TODO: Mappe om til Behovssekvens
                        val behovssekvens: Behovssekvens = cleanupMelding.tilK9Behovssekvens()

                        logger.info("Videresender journalført dele omsorgsdager til K9-Sak")
                        logger.info("Melding som blir sendt til K9: {}", cleanupMelding.serialiserTilData()) //TODO: Fjernes, kun for debug
                        cleanupMelding.journalførtMelding.serialiserTilData()
                    }
                }
                .to(tilJournalfort.name, tilJournalfort.produced) //TODO: Sende til K9-sak
            return builder.build()
        }
    }
    internal fun stop() = stream.stop(becauseOfError = false)
}

private fun CleanupDeleOmsorgsdager.tilK9Behovssekvens(): Behovssekvens {
    val id: String = ULID().nextULID()
    val correlationId = this.metadata.correlationId
    val journalPostIdListe = listOf<String>(this.journalførtMelding.journalpostId)

    val melding = this.meldingV1

    val overførerFra: OverføreOmsorgsdagerBehov.OverførerFra = OverføreOmsorgsdagerBehov.OverførerFra(
        identitetsnummer = melding.søker.aktørId,
        jobberINorge = melding.arbeidINorge,
        borINorge = melding.borINorge
    )

    val overførerTil: OverføreOmsorgsdagerBehov.OverførerTil = OverføreOmsorgsdagerBehov.OverførerTil(
        identitetsnummer = melding.mottakerFnr,
        relasjon = melding.mottakerType.tilK9Rapid(),
        harBoddSammenMinstEttÅr = true
    )

    val omsorgsdagerTattUtIÅr = melding.antallDagerBruktEtter1Juli
    val omsorgsdagerÅOverføre = melding.antallDagerSomSkalOverføres
    val kilde = OverføreOmsorgsdagerBehov.Kilde.Digital

    //TODO Noe for å mappe om de to ulike listene med Barn og AndreBarn til en felles liste av K9Barn
    val listeOverBarn = tilK9Barn(melding.harAleneomsorgFor, melding.harUtvidetRettFor)

    val behovssekvens: Behovssekvens = Behovssekvens(
        id = id,
        correlationId = correlationId,
        behov = *arrayOf(
            OverføreOmsorgsdagerBehov(
                fra = overførerFra,
                til = overførerTil,
                omsorgsdagerTattUtIÅr = omsorgsdagerTattUtIÅr,
                omsorgsdagerÅOverføre = omsorgsdagerÅOverføre,
                barn = listeOverBarn,
                kilde = kilde,
                journalpostIder = journalPostIdListe
            )
        )
    )

    return behovssekvens
}

internal fun tilK9Barn(harAleneomsorgFor: BarnOgAndreBarn, harUtvidetRettFor: BarnOgAndreBarn) : List<OverføreOmsorgsdagerBehov.Barn>{
    var listeOverBarn: MutableList<OverføreOmsorgsdagerBehov.Barn> = mutableListOf()

    val barnMedAleneomsorg = harAleneomsorgFor.tilK9Barn(harAleneomsorg = true)
    val barnMedUtvidetRett = harUtvidetRettFor.tilK9Barn(harUtvidetRett = true)

    barnMedAleneomsorg.forEach {
        listeOverBarn.leggTilBarn(it)
    }

    barnMedUtvidetRett.forEach {
        listeOverBarn.leggTilBarn(it)
    }

    return listeOverBarn
}

private fun MutableList<OverføreOmsorgsdagerBehov.Barn>.leggTilBarn(nyttBarn: OverføreOmsorgsdagerBehov.Barn) {

    forEachIndexed { index, barnIListen ->
        if(nyttBarn erSammeBarnSom barnIListen){
            val barnMedAleneomsorgOgUtvidetRett = barnIListen.copy(
                aleneOmOmsorgen = true,
                utvidetRett = true
            )
            this.removeAt(index)
            this.add(barnMedAleneomsorgOgUtvidetRett)
            return
        }
    }

    this.add(nyttBarn)
}

internal infix fun OverføreOmsorgsdagerBehov.Barn.erSammeBarnSom(barn: OverføreOmsorgsdagerBehov.Barn) =
    (identitetsnummer == barn.identitetsnummer && fødselsdato == barn.fødselsdato)


internal fun BarnOgAndreBarn.tilK9Barn(harUtvidetRett: Boolean = false, harAleneomsorg: Boolean = false): MutableList<OverføreOmsorgsdagerBehov.Barn> {
    var listeOverBarn: MutableList<OverføreOmsorgsdagerBehov.Barn> = mutableListOf()

    barn.forEach {
        listeOverBarn.add(
            OverføreOmsorgsdagerBehov.Barn(
                identitetsnummer = it.aktørId.toString(),
                fødselsdato = it.fødselsdato,
                aleneOmOmsorgen = harAleneomsorg,
                utvidetRett = harUtvidetRett
            )
        )
    }

    andreBarn.forEach {
        listeOverBarn.add(
            OverføreOmsorgsdagerBehov.Barn(
                identitetsnummer = it.fnr,
                fødselsdato = it.fødselsdato,
                aleneOmOmsorgen = harAleneomsorg,
                utvidetRett = harUtvidetRett
            )
        )
    }

    return listeOverBarn
}