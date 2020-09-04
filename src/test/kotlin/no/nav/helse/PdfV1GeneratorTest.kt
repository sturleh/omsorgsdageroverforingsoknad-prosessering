package no.nav.helse

import no.nav.helse.prosessering.v1.*
import no.nav.helse.prosessering.v1.deleOmsorgsdager.AndreBarn
import no.nav.helse.prosessering.v1.deleOmsorgsdager.Barn
import no.nav.helse.prosessering.v1.deleOmsorgsdager.MeldingDeleOmsorgsdagerV1
import no.nav.helse.prosessering.v1.deleOmsorgsdager.OverføreTilType
import no.nav.helse.prosessering.v1.overforeDager.*
import org.junit.Ignore
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class PdfV1GeneratorTest {

    private companion object {
        private val generator = PdfV1Generator()
        private val fødselsdato = LocalDate.now()
    }

    private fun gyldigSoknadOverforeDager() = SøknadOverføreDagerV1(
        språk = "nb",
        antallDager = 5,
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        arbeidssituasjon = listOf(Arbeidssituasjon.ARBEIDSTAKER, Arbeidssituasjon.FRILANSER, Arbeidssituasjon.SELVSTENDIGNÆRINGSDRIVENDE),
        søknadId = "Overføre dager",
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Utenlandsopphold(
                    LocalDate.of(2020, 1, 2),
                    LocalDate.of(2020, 1, 3),
                    "US", "USA"
                )
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.of(2020,2,1),
                    tilOgMed = LocalDate.of(2020,2,24),
                    landkode = "US",
                    landnavn = "USA"
                )
            )
        ),
        fnrMottaker = "123456789",
        navnMottaker = "Navn Navnesen Mottaker",
        mottatt = ZonedDateTime.now(),
        søker = Søker(
            aktørId = "123456",
            fornavn = "Ærling",
            mellomnavn = "Øverbø",
            etternavn = "Ånsnes",
            fødselsnummer = "29099012345",
            fødselsdato = fødselsdato
        ),
        fosterbarn = listOf(
            Fosterbarn("29099012345"),
            Fosterbarn("02119970078")
        ),
        stengingsperiode = Stengingsperiode.ETTER_AUGUST_9
    )

    fun gyldigMeldingDeleOmsorgsdager() = MeldingDeleOmsorgsdagerV1(
        søknadId = UUID.randomUUID().toString(),
        språk = "nb",
        søker = Søker(
            aktørId = "123456",
            fornavn = "Ærling",
            mellomnavn = "Øverbø",
            etternavn = "Ånsnes",
            fødselsnummer = "12345678911",
            fødselsdato = LocalDate.now().minusYears(18)
        ),
        mottatt = ZonedDateTime.now(),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        andreBarn = listOf(
            AndreBarn(
                fnr = "12345678900",
                navn = "Barn Barnesen",
                ingenFnr = false
            )
        ),
        harAleneomsorg = true,
        harAleneomsorgFor = listOf(
            Barn(
                fødselsdato = LocalDate.parse("2010-01-01"),
                aktørId = "12345",
                fornavn = "Fornavn",
                etternavn = "Etternavn",
                mellomnavn = "Mellomnavn"
            )
        ),
        harUtvidetRett = true,
        harUtvidetRettFor = listOf(
            Barn(
                fødselsdato = LocalDate.parse("2010-01-01"),
                aktørId = "12345",
                fornavn = "Fornavn",
                etternavn = "Etternavn",
                mellomnavn = "Mellomnavn"
            )
        ),
        borINorge = true,
        arbeidINorge = true,
        arbeidssituasjon = listOf(
            Arbeidssituasjon.ARBEIDSTAKER
        ),
        antallDagerHarBruktEtter1Juli = 10,
        harDeltDagerMedAndreTidligere = true,
        antallDagerHarDeltMedAndre = 10,
        overføreTilType = OverføreTilType.NY_EKTEFELLE,
        fnrMottaker = "12345678911",
        navnMottaker = "Navn Mottaker",
        antallDagerTilOverføre = 5,
        harBekreftetMottakerOpplysninger = true,
        feltForTest = "test"
    )

    private fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-søknad-overfore-dager"
        var pdf = generator.generateSoknadOppsummeringPdfOverforeDager(
            melding = gyldigSoknadOverforeDager()
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "2-full-søknad-dele-omsorgsdager"
        pdf = generator.generateSoknadOppsummeringPdfDeleOmsorgsdager(
            melding = gyldigMeldingDeleOmsorgsdager()
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)
    }

    private fun pdfPath(soknadId: String) = "${System.getProperty("user.dir")}/generated-pdf-$soknadId.pdf"

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    @Ignore
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }
}
