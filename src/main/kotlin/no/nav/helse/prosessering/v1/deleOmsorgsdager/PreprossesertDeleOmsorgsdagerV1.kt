package no.nav.helse.prosessering.v1.deleOmsorgsdager

import no.nav.helse.aktoer.AktørId
import no.nav.helse.prosessering.v1.overforeDager.Arbeidssituasjon
import no.nav.helse.prosessering.v1.overforeDager.PreprossesertSøker
import java.net.URI
import java.time.ZonedDateTime

data class PreprossesertDeleOmsorgsdagerV1(
    val soknadId: String,
    val mottatt: ZonedDateTime,
    val søker: PreprossesertSøker,
    val språk: String?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val dokumentUrls: List<List<URI>>,
    val andreBarn: List<AndreBarn>? = listOf(),
    val harAleneomsorg: Boolean?,
    val harAleneomsorgFor: List<Barn>? = listOf(),
    val harUtvidetRett: Boolean?,
    val harUtvidetRettFor: List<Barn>? = listOf(),
    val borINorge: Boolean?,
    val arbeidINorge: Boolean?,
    val arbeidssituasjon: List<Arbeidssituasjon>?,
    val antallDagerHarBruktEtter1Juli: Int,
    val harDeltDagerMedAndreTidligere: Boolean,
    val antallDagerHarDeltMedAndre: Int?,
    val overføreTilType: OverføreTilType?,
    val fnrMottaker: String?,
    val navnMottaker: String?,
    val antallDagerTilOverføre: Int?,
    val harBekreftetMottakerOpplysninger: Boolean?
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
        dokumentUrls = dokumentUrls,
        andreBarn = melding.andreBarn,
        harAleneomsorg = melding.harAleneomsorg,
        harAleneomsorgFor = melding.harAleneomsorgFor,
        harUtvidetRett = melding.harUtvidetRett,
        harUtvidetRettFor = melding.harUtvidetRettFor,
        borINorge = melding.borINorge,
        arbeidINorge = melding.arbeidINorge,
        arbeidssituasjon = melding.arbeidssituasjon,
        antallDagerHarBruktEtter1Juli = melding.antallDagerHarBruktEtter1Juli,
        harDeltDagerMedAndreTidligere = melding.harDeltDagerMedAndreTidligere,
        antallDagerHarDeltMedAndre = melding.antallDagerHarDeltMedAndre,
        overføreTilType = melding.overføreTilType,
        fnrMottaker = melding.fnrMottaker,
        navnMottaker = melding.navnMottaker,
        antallDagerTilOverføre = melding.antallDagerTilOverføre,
        harBekreftetMottakerOpplysninger = melding.harBekreftetMottakerOpplysninger
    )
}


