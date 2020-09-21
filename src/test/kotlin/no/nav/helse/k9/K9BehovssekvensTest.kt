package no.nav.helse.k9

import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.tilK9BarnListe
import no.nav.helse.prosessering.v1.deleOmsorgsdager.BarnUtvidet
import no.nav.helse.prosessering.v1.deleOmsorgsdager.Mottaker
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class K9BehovssekvensTest {

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

        assertEquals(barn.identitetsnummer, k9Barn.identitetsnummer)
        assertEquals(barn.aleneOmOmsorgen, k9Barn.aleneOmOmsorgen)
        assertEquals(barn.utvidetRett, k9Barn.utvidetRett)
        assertEquals(barn.fødselsdato, k9Barn.fødselsdato)
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

        assertEquals(listeOverBarn[0].identitetsnummer, listeOverK9Barn[0].identitetsnummer)
        assertEquals(listeOverBarn[0].fødselsdato, listeOverK9Barn[0].fødselsdato)

        assertEquals(listeOverBarn[1].identitetsnummer, listeOverK9Barn[1].identitetsnummer)
        assertEquals(listeOverBarn[1].fødselsdato, listeOverK9Barn[1].fødselsdato)
    }

}