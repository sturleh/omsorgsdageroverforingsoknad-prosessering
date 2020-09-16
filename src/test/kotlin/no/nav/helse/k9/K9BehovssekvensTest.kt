package no.nav.helse.k9

import no.nav.helse.omsorgsdageroverførningKonfigurertMapper
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.erSammeBarnSom
import no.nav.helse.prosessering.v1.asynkron.deleOmsorgsdager.tilK9Barn
import no.nav.helse.prosessering.v1.deleOmsorgsdager.AndreBarn
import no.nav.helse.prosessering.v1.deleOmsorgsdager.Barn
import no.nav.helse.prosessering.v1.deleOmsorgsdager.BarnOgAndreBarn
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import kotlin.test.assertTrue

class K9BehovssekvensTest {

    @Test
    fun `Test at Barn og AndreBarn blir mappet riktig til liste over K9Rapid Barn`(){
        val listeOverBarn = BarnOgAndreBarn(
            barn = listOf(
                Barn(
                    fødselsdato = LocalDate.parse("2020-01-01"),
                    fornavn = "Barn",
                    mellomnavn = "Mellomnavn",
                    etternavn = "Barnesen",
                    aktørId = "22222"
                )
            ),
            andreBarn = listOf(
                AndreBarn(
                    fnr = "11111",
                    navn = "Annet Barn",
                    fødselsdato = LocalDate.parse("2020-02-02")
                )
            )
        )

        val listeOverK9RapidBarn = listeOverBarn.tilK9Barn()
        val barnSomJson = listeOverK9RapidBarn.somJson()

        val forventet =
            //language=json
            """
                [
                  {
                    "identitetsnummer": "22222",
                    "fødselsdato": "2020-01-01",
                    "aleneOmOmsorgen": false,
                    "utvidetRett": false
                  },
                  {
                    "identitetsnummer": "11111",
                    "fødselsdato": "2020-02-02",
                    "aleneOmOmsorgen": false,
                    "utvidetRett": false
                  }
                ]
        """.trimIndent()

        JSONAssert.assertEquals(forventet, barnSomJson, true)
    }
    @Test
    fun `Tester at erSammeBarnSom fungerer`(){
        val barn = OverføreOmsorgsdagerBehov.Barn(
            identitetsnummer = "12345",
            fødselsdato = LocalDate.parse("2020-01-01"),
            aleneOmOmsorgen = true,
            utvidetRett = true
        )

        val kopiAvBarn = barn.copy()

        assertTrue(kopiAvBarn erSammeBarnSom barn)
    }

    @Test
    fun `Test at dersom samme barn finnes i listen med aleneomsorg og utvidet rett så skal det slåes sammen til et barn med true for begge verdiene`(){
        val listeOverBarnMedAleneomsorg = barnOgAndreBarn()
        val listeOverBarnMedUtvidetRett = BarnOgAndreBarn(
            barn = listOf(),
            andreBarn = listOf(
                AndreBarn(
                    fnr = "222",
                    navn = "Annet Barn",
                    fødselsdato = LocalDate.parse("2020-02-02")
                ),
                AndreBarn(
                    fnr = "333",
                    navn = "HarKunUtvidetRett",
                    fødselsdato = LocalDate.parse("2020-03-03")
                )
            )
        )

        val listeOverBarnK9Rapid = tilK9Barn(
            harAleneomsorgFor = listeOverBarnMedAleneomsorg,
            harUtvidetRettFor = listeOverBarnMedUtvidetRett
        )

        val barnSomJson = listeOverBarnK9Rapid.somJson()

        val forventet =
            //language=json
            """
                [
                  {
                    "identitetsnummer": "111",
                    "fødselsdato": "2020-01-01",
                    "aleneOmOmsorgen": true,
                    "utvidetRett": false
                  },
                  {
                    "identitetsnummer": "222",
                    "fødselsdato": "2020-02-02",
                    "aleneOmOmsorgen": true,
                    "utvidetRett": true
                  },
                  {
                    "identitetsnummer": "333",
                    "fødselsdato": "2020-03-03",
                    "aleneOmOmsorgen": false,
                    "utvidetRett": true
                  }
                  
                ]
        """.trimIndent()

        JSONAssert.assertEquals(forventet, barnSomJson, true)

    }

    private fun barnOgAndreBarn() = BarnOgAndreBarn(
        barn = listOf(
            Barn(
                fødselsdato = LocalDate.parse("2020-01-01"),
                fornavn = "Barn",
                mellomnavn = "Mellomnavn",
                etternavn = "Barnesen",
                aktørId = "111"
            )
        ),
        andreBarn = listOf(
            AndreBarn(
                fnr = "222",
                navn = "Annet Barn",
                fødselsdato = LocalDate.parse("2020-02-02")
            )
        )
    )

    internal fun Any.somJson() = omsorgsdageroverførningKonfigurertMapper().writeValueAsString(this)

}