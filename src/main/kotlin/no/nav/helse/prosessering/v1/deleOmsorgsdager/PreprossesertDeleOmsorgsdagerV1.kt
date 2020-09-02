package no.nav.helse.prosessering.v1.deleOmsorgsdager

import no.nav.helse.aktoer.AktørId
import no.nav.helse.prosessering.v1.*
import no.nav.helse.prosessering.v1.overforeDager.Medlemskap
import no.nav.helse.prosessering.v1.overforeDager.PreprossesertSøker
import no.nav.k9.søknad.omsorgspenger.overføring.Mottaker
import java.net.URI
import java.time.ZonedDateTime

data class PreprossesertDeleOmsorgsdagerV1(
    val soknadId: String,
    val mottatt: ZonedDateTime,
    val søker: PreprossesertSøker,
    val språk: String?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val dokumentUrls: List<List<URI>>
    ) {
    internal constructor(
        melding: MeldingDeleOmsorgsdagerV1,
        søkerAktørId: AktørId,
        dokumentUrls: List<List<URI>>
    ) : this(
        språk = melding.språk,
        soknadId = melding.søknadId,
        søker = PreprossesertSøker(melding.søker, søkerAktørId),
        mottatt = melding.mottatt,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        dokumentUrls = dokumentUrls
    )
}


