package no.nav.helse.k9

import no.nav.helse.aktoer.AktørId
import no.nav.helse.prosessering.Metadata
import no.nav.helse.prosessering.v1.asynkron.CleanupDeleOmsorgsdager
import no.nav.helse.prosessering.v1.asynkron.JournalfortDeleOmsorgsdager
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.tilK9BarnListe
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.tilK9Behovssekvens
import no.nav.helse.prosessering.v1.deleOmsorgsdager.BarnUtvidet
import no.nav.helse.prosessering.v1.deleOmsorgsdager.MeldingDeleOmsorgsdagerV1
import no.nav.helse.prosessering.v1.deleOmsorgsdager.Mottaker
import no.nav.helse.prosessering.v1.deleOmsorgsdager.PreprosessertDeleOmsorgsdagerV1
import no.nav.helse.prosessering.v1.overforeDager.Arbeidssituasjon
import no.nav.helse.prosessering.v1.overforeDager.Søker
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import org.json.JSONObject
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class K9BehovssekvensTest {

    companion object{
        val gyldigFodselsnummerA = "02119970078"
        val gyldigFodselsnummerB = "19066672169"
        val gyldigBarnFødselsnummer = "16012098999"
        val dNummerA = "55125314561"
    }

    @Test
    fun `Mottakertype blir omgjort til riktig K9 relasjon`(){
        val samboer = Mottaker.SAMBOER
        assertEquals(samboer.tilK9Relasjon(), OverføreOmsorgsdagerBehov.Relasjon.NåværendeSamboer)

        val ektefelle = Mottaker.EKTEFELLE
        assertEquals(ektefelle.tilK9Relasjon(), OverføreOmsorgsdagerBehov.Relasjon.NåværendeEktefelle)
    }

    @Test
    fun `Barn blir riktig gjort om til k9Barn`(){
        val barn = BarnUtvidet(
                identitetsnummer = "12345",
                aktørId = "1234",
                navn = "Ola",
                fødselsdato = LocalDate.parse("2020-01-01"),
                aleneOmOmsorgen = true,
                utvidetRett = false
        )

        val k9Barn = barn.tilK9Barn()

        assertTrue(barn erSammeBarnSom k9Barn)
    }

    @Test
    fun `Liste over barn blir til riktig liste med k9Barn`(){
        val listeOverBarn = listOf(
            BarnUtvidet(
                identitetsnummer = "12345",
                aktørId = "1234",
                navn = "Ola",
                fødselsdato = LocalDate.parse("2020-01-01"),
                aleneOmOmsorgen = true,
                utvidetRett = false
            ),
            BarnUtvidet(
                identitetsnummer = "54321",
                aktørId = "4321",
                navn = "Kjell",
                fødselsdato = LocalDate.parse("2020-01-01"),
                aleneOmOmsorgen = true,
                utvidetRett = true
            )
        )

        val listeOverK9Barn = listeOverBarn.tilK9BarnListe()

        assertTrue(listeOverBarn[0] erSammeBarnSom listeOverK9Barn[0])
        assertTrue(listeOverBarn[1] erSammeBarnSom listeOverK9Barn[1])
        assertEquals(listeOverBarn.size, listeOverK9Barn.size)
    }

    @Test
    fun `Verifisere at full cleanupMelding blir omgjort til riktig behovssekvensformat`(){
        val cleanupDeleOmsorgsdager = CleanupDeleOmsorgsdager(
            metadata = Metadata(
                version = 1,
                correlationId = "12345678910",
                requestId = "1111111111"
            ),
            meldingV1 = PreprosessertDeleOmsorgsdagerV1(
                melding = MeldingDeleOmsorgsdagerV1(
                    språk = "nb",
                    søknadId = UUID.randomUUID().toString(),
                    mottatt = ZonedDateTime.parse("2020-01-01T12:00:00.000Z"),
                    søker = Søker(
                        aktørId = "1234",
                        fødselsnummer = gyldigFodselsnummerA,
                        fødselsdato = LocalDate.now().minusDays(1000),
                        etternavn = "Nordmann",
                        mellomnavn = "Mellomnavn",
                        fornavn = "Ola"
                    ),
                    id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                    harBekreftetOpplysninger = true,
                    harForståttRettigheterOgPlikter = true,
                    barn = listOf(
                        BarnUtvidet(
                            identitetsnummer = "$gyldigBarnFødselsnummer",
                            aktørId = "1234",
                            fødselsdato = LocalDate.parse("2020-01-01"),
                            navn = "Kjell",
                            aleneOmOmsorgen = true,
                            utvidetRett = false
                        ),
                        BarnUtvidet(
                            identitetsnummer = "$gyldigBarnFødselsnummer",
                            aktørId = "1234",
                            fødselsdato = LocalDate.parse("2020-02-02"),
                            navn = "Kjell",
                            aleneOmOmsorgen = true,
                            utvidetRett = true
                        )
                    ),
                    borINorge = true,
                    arbeiderINorge = true,
                    arbeidssituasjon = listOf(
                        Arbeidssituasjon.ARBEIDSTAKER
                    ),
                    antallDagerBruktIÅr = 3,
                    mottakerType = Mottaker.SAMBOER,
                    mottakerFnr = gyldigFodselsnummerB,
                    mottakerNavn = "Navn Mottaker",
                    antallDagerSomSkalOverføres = 5
                ),
                søkerAktørId = AktørId("1234"),
                dokumentUrls = listOf()
            ),
            journalførtMelding = JournalfortDeleOmsorgsdager(journalpostId = "123")
        )

        val (id, løsning) = cleanupDeleOmsorgsdager.tilK9Behovssekvens().keyValue
        val løsningSomJson = JSONObject(løsning)

        assertEquals(løsningSomJson.getJSONArray("@behovsrekkefølge").getString(0), "OverføreOmsorgsdager")

        val overføreOmsorgsdagerJson = løsningSomJson.getJSONObject("@behov").getJSONObject("OverføreOmsorgsdager")
        assertEquals(overføreOmsorgsdagerJson.getJSONObject("fra").getString("identitetsnummer"), gyldigFodselsnummerA)
        assertEquals(overføreOmsorgsdagerJson.getJSONObject("fra").getBoolean("jobberINorge"), true)
        assertEquals(overføreOmsorgsdagerJson.getJSONObject("fra").getBoolean("borINorge"), true)

        assertEquals(overføreOmsorgsdagerJson.getJSONObject("til").getString("identitetsnummer"), gyldigFodselsnummerB)
        assertEquals(overføreOmsorgsdagerJson.getJSONObject("til").getString("relasjon"), "NåværendeSamboer")
        assertEquals(overføreOmsorgsdagerJson.getJSONObject("til").getBoolean("harBoddSammenMinstEttÅr"), true)

        assertEquals(overføreOmsorgsdagerJson.getInt("omsorgsdagerTattUtIÅr"), 3)
        assertEquals(overføreOmsorgsdagerJson.getInt("omsorgsdagerÅOverføre"), 5)
        assertEquals(overføreOmsorgsdagerJson.getString("kilde"), "Digital")

        val barnSomJson = overføreOmsorgsdagerJson.getJSONArray("barn")
        assertEquals(barnSomJson.getJSONObject(0).getString("identitetsnummer"), "$gyldigBarnFødselsnummer")
        assertEquals(barnSomJson.getJSONObject(0).getString("fødselsdato"), "2020-01-01")
        assertEquals(barnSomJson.getJSONObject(0).getBoolean("aleneOmOmsorgen"), true)
        assertEquals(barnSomJson.getJSONObject(0).getBoolean("utvidetRett"), false)

        assertEquals(barnSomJson.getJSONObject(1).getString("identitetsnummer"), "$gyldigBarnFødselsnummer")
        assertEquals(barnSomJson.getJSONObject(1).getString("fødselsdato"), "2020-02-02")
        assertEquals(barnSomJson.getJSONObject(1).getBoolean("aleneOmOmsorgen"), true)
        assertEquals(barnSomJson.getJSONObject(1).getBoolean("utvidetRett"), true)
    }

    private infix fun BarnUtvidet.erSammeBarnSom(k9Barn: OverføreOmsorgsdagerBehov.Barn): Boolean{
        return  this.identitetsnummer == k9Barn.identitetsnummer &&
                this.fødselsdato == k9Barn.fødselsdato &&
                this.utvidetRett == k9Barn.utvidetRett &&
                this.aleneOmOmsorgen == k9Barn.aleneOmOmsorgen

    }
}