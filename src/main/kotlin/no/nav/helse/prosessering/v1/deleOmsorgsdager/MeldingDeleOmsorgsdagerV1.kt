package no.nav.helse.prosessering.v1.deleOmsorgsdager

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.prosessering.v1.overforeDager.Arbeidssituasjon
import no.nav.helse.prosessering.v1.overforeDager.Søker
import java.time.LocalDate
import java.time.ZonedDateTime

data class MeldingDeleOmsorgsdagerV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val andreBarn: List<AndreBarn>? = listOf(),
    val harAleneomsorg: Boolean,
    val harAleneomsorgFor: List<Barn>? = listOf(),
    val harUtvidetRett: Boolean,
    val harUtvidetRettFor: List<Barn>? = listOf(),
    val borINorge: Boolean,
    val arbeidINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val antallDagerHarBruktEtter1Juli: Int,
    val harDeltDagerMedAndreTidligere: Boolean,
    val antallDagerHarDeltMedAndre: Int,
    val overføreTilType: OverføreTilType,
    val fnrMottaker: String,
    val navnMottaker: String,
    val antallDagerTilOverføre: Int,
    val harBekreftetMottakerOpplysninger: Boolean
)

data class Barn (
    val fødselsdato: LocalDate,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val aktørId: String?
)

data class AndreBarn (
    val fnr: String?,
    val ingenFnr: Boolean,
    val navn: String
)

enum class OverføreTilType() {
    @JsonProperty("nyEktefelle") NY_EKTEFELLE,
    @JsonProperty("nySamboerHarBoddSammenMinst12maneder") NY_SAMBOER_HAR_BODD_SAMMEN_MINST_12_MÅNEDER
}