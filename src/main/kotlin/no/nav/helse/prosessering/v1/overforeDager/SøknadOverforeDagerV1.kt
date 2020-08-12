package no.nav.helse.prosessering.v1.overforeDager

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.ZonedDateTime

data class SøknadOverføreDagerV1 (
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String? = "nb",
    val antallDager: Int,
    val fnrMottaker: String,
    val navnMottaker: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val fosterbarn: List<Fosterbarn>? = listOf(),
    val stengingsperiode: Stengingsperiode? = null //TODO Fjerne optional etter frontend har prodsatt
)

data class Fosterbarn(
    val fødselsnummer: String
)

enum class Arbeidssituasjon(val utskriftvennlig: String) {
    @JsonProperty("arbeidstaker") ARBEIDSTAKER("Arbeidstaker"),
    @JsonProperty("selvstendigNæringsdrivende") SELVSTENDIGNÆRINGSDRIVENDE("Selvstendig næringsdrivende"),
    @JsonProperty("frilanser") FRILANSER("Frilanser")
}

data class Søker(
    val fødselsnummer: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate?,
    val aktørId: String
) {
    override fun toString(): String {
        return "Soker(fornavn='$fornavn', mellomnavn=$mellomnavn, etternavn='$etternavn', fødselsdato=$fødselsdato, aktørId='$aktørId')"
    }
}

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean,
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean,
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
)

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn')"
    }
}

enum class Stengingsperiode(val utskriftsvennlig: String){
    @JsonProperty("mars13tilJuni30") MARS_13_TIL_JUNI_30("13. mars - 30. juni"),
    @JsonProperty("etterAugust9") ETTER_AUGUST_9("Fra og med 10. august")
}