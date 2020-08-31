package no.nav.helse.prosessering.v1.asynkron

import no.nav.helse.dokument.DokumentService
import no.nav.helse.joark.JoarkGateway
import no.nav.helse.kafka.KafkaConfig
import no.nav.helse.prosessering.v1.PreprosseseringV1Service
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.CleanupStreamDeleOmsorgsdager
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.JournalforingsStreamDeleOmsorgsdager
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.PreprosseseringStreamDeleOmsorgsdager
import no.nav.helse.prosessering.v1.asynkron.overforeDager.CleanupStreamOverforeDager
import no.nav.helse.prosessering.v1.asynkron.overforeDager.JournalforingsStreamOverforeDager
import no.nav.helse.prosessering.v1.asynkron.overforeDager.PreprosseseringStreamOverforeDager
import org.slf4j.LoggerFactory

internal class AsynkronProsesseringV1Service(
    kafkaConfig: KafkaConfig,
    preprosseseringV1Service: PreprosseseringV1Service,
    joarkGateway: JoarkGateway,
    dokumentService: DokumentService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(AsynkronProsesseringV1Service::class.java)
    }

    private val preprosseseringStreamOverforeDager = PreprosseseringStreamOverforeDager(
        kafkaConfig = kafkaConfig,
        preprosseseringV1Service = preprosseseringV1Service
    )

    private val journalforingsStreamOverforeDager = JournalforingsStreamOverforeDager(
        kafkaConfig = kafkaConfig,
        joarkGateway = joarkGateway
    )

    private val cleanupStreamOverforeDager = CleanupStreamOverforeDager(
        kafkaConfig = kafkaConfig,
        dokumentService = dokumentService
    )

    private val preprosseseringStreamDeleOmsorgsdager = PreprosseseringStreamDeleOmsorgsdager(
        kafkaConfig = kafkaConfig,
        preprosseseringV1Service = preprosseseringV1Service
    )

    private val journalforingsStreamDeleOmsorgsdager = JournalforingsStreamDeleOmsorgsdager(
        kafkaConfig = kafkaConfig,
        joarkGateway = joarkGateway
    )

    private val cleanupStreamDeleOmsorgsdager = CleanupStreamDeleOmsorgsdager(
        kafkaConfig = kafkaConfig,
        dokumentService = dokumentService
    )

    private val healthChecks = setOf(
        preprosseseringStreamOverforeDager.healthy,
        journalforingsStreamOverforeDager.healthy,
        cleanupStreamOverforeDager.healthy,
        preprosseseringStreamDeleOmsorgsdager.healthy,
        journalforingsStreamDeleOmsorgsdager.healthy,
        cleanupStreamDeleOmsorgsdager.healthy
    )

    private val isReadyChecks = setOf(
        preprosseseringStreamOverforeDager.ready,
        journalforingsStreamOverforeDager.ready,
        cleanupStreamOverforeDager.ready,
        preprosseseringStreamDeleOmsorgsdager.ready,
        journalforingsStreamDeleOmsorgsdager.ready,
        cleanupStreamDeleOmsorgsdager.ready
    )

    internal fun stop() {
        logger.info("Stopper streams.")
        preprosseseringStreamOverforeDager.stop()
        journalforingsStreamOverforeDager.stop()
        cleanupStreamOverforeDager.stop()
        preprosseseringStreamDeleOmsorgsdager.stop()
        journalforingsStreamDeleOmsorgsdager.stop()
        cleanupStreamDeleOmsorgsdager.stop()
        logger.info("Alle streams stoppet.")
    }

    internal fun healthChecks() = healthChecks
    internal fun isReadyChecks() = isReadyChecks
}