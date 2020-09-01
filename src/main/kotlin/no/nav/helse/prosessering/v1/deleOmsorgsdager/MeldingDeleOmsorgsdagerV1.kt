package no.nav.helse.prosessering.v1.deleOmsorgsdager

import no.nav.helse.prosessering.v1.overforeDager.Medlemskap
import no.nav.helse.prosessering.v1.overforeDager.Søker
import java.time.ZonedDateTime

data class MeldingDeleOmsorgsdagerV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)