package no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager

import no.nav.helse.CorrelationId
import no.nav.helse.aktoer.AktørId
import no.nav.helse.dokument.DokumentService
import no.nav.helse.kafka.KafkaConfig
import no.nav.helse.kafka.ManagedKafkaStreams
import no.nav.helse.kafka.ManagedStreamHealthy
import no.nav.helse.kafka.ManagedStreamReady
import no.nav.helse.prosessering.v1.asynkron.*
import no.nav.helse.prosessering.v1.asynkron.Topics
import no.nav.helse.prosessering.v1.asynkron.process
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
            val tilJournalfort= Topics.JOURNALFORT_DELE_OMSORGSDAGER //TODO: Lage egen topic som går til K9-Sak

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
