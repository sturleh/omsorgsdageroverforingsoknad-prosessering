package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import no.nav.common.KafkaEnvironment
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.k9.assertDeleOmsorgsdagerFormat
import no.nav.helse.prosessering.v1.deleOmsorgsdager.*
import no.nav.helse.prosessering.v1.overforeDager.Arbeidssituasjon
import no.nav.helse.prosessering.v1.overforeDager.Søker
import org.junit.AfterClass
import org.junit.BeforeClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals


@KtorExperimentalAPI
class DeleOmsorgsdagerProsesseringTest {

    @KtorExperimentalAPI
    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(DeleOmsorgsdagerProsesseringTest::class.java)

        private val wireMockServer: WireMockServer = WireMockBuilder()
            .withNaisStsSupport()
            .withAzureSupport()
            .navnOppslagConfig()
            .build()
            .stubK9DokumentHealth()
            .stubOmsorgspengerJoarkHealth()
            .stubJournalfor()
            .stubLagreDokument()
            .stubSlettDokument()
            .stubAktørRegister("29099012345", "123456")

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaTestProducerDeleOmsorgsdager = kafkaEnvironment.meldingDeleOmsorgsdagerProducer()

        private val journalføringsKonsumerDeleOmsorgsdager = kafkaEnvironment.journalføringsKonsumerDeleOmsorgsdager()

        // Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
        private val gyldigFodselsnummerA = "02119970078"
        private val gyldigFodselsnummerB = "19066672169"
        private val dNummerA = "55125314561"

        private var engine = newEngine(kafkaEnvironment).apply {
            start(wait = true)
        }

        private fun getConfig(kafkaEnvironment: KafkaEnvironment?): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)
            return HoconApplicationConfig(mergedConfig)
        }

        private fun newEngine(kafkaEnvironment: KafkaEnvironment?) = TestApplicationEngine(createTestEnvironment {
            config = getConfig(kafkaEnvironment)
        })

        private fun stopEngine() = engine.stop(5, 60, TimeUnit.SECONDS)

        internal fun restartEngine() {
            stopEngine()
            engine = newEngine(kafkaEnvironment)
            engine.start(wait = true)
        }

        @BeforeClass
        @JvmStatic
        fun buildUp() {
            wireMockServer.stubAktørRegister(gyldigFodselsnummerA, "666666666")
            wireMockServer.stubAktørRegister(gyldigFodselsnummerB, "777777777")
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            journalføringsKonsumerDeleOmsorgsdager.close()
            kafkaTestProducerDeleOmsorgsdager.close()
            stopEngine()
            kafkaEnvironment.tearDown()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun`Gyldig melding om deling av omsorgsdager blir prosessert av journalføringkonsumer`(){
        val søknad = gyldigMeldingDeleOmsorgsdager(
            fødselsnummerSoker = gyldigFodselsnummerA,
            sprak = "nb"
        )

        kafkaTestProducerDeleOmsorgsdager.leggTilMottakDeleOmsorgsdager(søknad)
        journalføringsKonsumerDeleOmsorgsdager
            .hentJournalførtMeldingDeleOmsorgsdager(søknad.søknadId)
            .assertDeleOmsorgsdagerFormat()
    }

    @Test
    fun `En feilprosessert melding om deling av omsorgsdager vil bli prosessert etter at tjenesten restartes`() {
        val melding = gyldigMeldingDeleOmsorgsdager(
            fødselsnummerSoker = gyldigFodselsnummerA
        )

        wireMockServer.stubJournalfor(500) // Simulerer feil ved journalføring

        kafkaTestProducerDeleOmsorgsdager.leggTilMottakDeleOmsorgsdager(melding)
        ventPaaAtRetryMekanismeIStreamProsessering()
        readyGir200HealthGir503()

        wireMockServer.stubJournalfor(201) // Simulerer journalføring fungerer igjen
        restartEngine()
        journalføringsKonsumerDeleOmsorgsdager
            .hentJournalførtMeldingDeleOmsorgsdager(melding.søknadId)
            .assertDeleOmsorgsdagerFormat()
    }

    private fun gyldigMeldingDeleOmsorgsdager(
        fødselsnummerSoker: String,
        sprak: String = "nb"
    ) : MeldingDeleOmsorgsdagerV1 = MeldingDeleOmsorgsdagerV1(
        språk = sprak,
        søknadId = UUID.randomUUID().toString(),
        mottatt = ZonedDateTime.now().plusDays(1),
        søker = Søker(
            aktørId = "$gyldigFodselsnummerA",
            fødselsnummer = fødselsnummerSoker,
            fødselsdato = LocalDate.now().minusDays(1000),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        andreBarn = listOf(
            AndreBarn(
                fnr = "$gyldigFodselsnummerA",
                fødselsdato = LocalDate.parse("2020-01-01"),
                navn = "Barn Barnesen"
            )
        ),
        harAleneomsorg = true,
        harAleneomsorgFor = BarnOgAndreBarn(
            barn = listOf(
                Barn(
                    fødselsdato = LocalDate.parse("2010-01-01"),
                    aktørId = "$gyldigFodselsnummerA",
                    fornavn = "Fornavn",
                    etternavn = "Etternavn",
                    mellomnavn = "Mellomnavn"
                )
            ),
            andreBarn = listOf()
        ),
        harUtvidetRett = true,
        harUtvidetRettFor = BarnOgAndreBarn(
            barn = listOf(
                Barn(
                    fødselsdato = LocalDate.parse("2010-01-01"),
                    aktørId = "$gyldigFodselsnummerA",
                    fornavn = "Fornavn",
                    etternavn = "Etternavn",
                    mellomnavn = "Mellomnavn"
                )
            ),
            andreBarn = listOf()
        ),
        borINorge = true,
        arbeidINorge = true,
        arbeidssituasjon = listOf(
            Arbeidssituasjon.ARBEIDSTAKER
        ),
        antallDagerBruktEtter1Juli = 10,
        mottakerType = Mottaker.EKTEFELLE,
        mottakerFnr = "$gyldigFodselsnummerB",
        mottakerNavn = "Navn Mottaker",
        antallDagerSomSkalOverføres = 5
    )

    private fun readyGir200HealthGir503() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/health") {}.apply {
                    assertEquals(HttpStatusCode.ServiceUnavailable, response.status())
                }
            }
        }
    }

    private fun ventPaaAtRetryMekanismeIStreamProsessering() = runBlocking { delay(Duration.ofSeconds(30)) }
}