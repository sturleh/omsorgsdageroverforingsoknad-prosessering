package no.nav.helse.prosessering.v1.deleOmsorgsdager

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.prosessering.v1.overforeDager.Arbeidssituasjon
import no.nav.helse.prosessering.v1.overforeDager.Søker
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import java.time.LocalDate
import java.time.ZonedDateTime

data class MeldingDeleOmsorgsdagerV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val andreBarn: List<AndreBarn>,
    val harAleneomsorg: Boolean,
    val harAleneomsorgFor: BarnOgAndreBarn,
    val harUtvidetRett: Boolean,
    val harUtvidetRettFor: BarnOgAndreBarn,
    val borINorge: Boolean,
    val arbeidINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val antallDagerBruktEtter1Juli: Int,
    val mottakerType: Mottaker,
    val mottakerFnr: String,
    val mottakerNavn: String,
    val antallDagerSomSkalOverføres: Int
)

data class Barn (
    val fødselsdato: LocalDate,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val aktørId: String?
)

data class BarnOgAndreBarn(
    val barn: List<Barn>,
    val andreBarn: List<AndreBarn>
)

data class AndreBarn (
    val fnr: String,
    val fødselsdato: LocalDate,
    val navn: String
)

enum class Mottaker(val utskriftsvennlig: String) {
    @JsonProperty("ektefelle") EKTEFELLE("Ektefelle"),
    @JsonProperty("samboer") SAMBOER("Samboer");

    fun tilK9Rapid(): OverføreOmsorgsdagerBehov.Relasjon {
        //TODO Sjekk om dette kan gjøres penere, kanskje annet navn? Enkel return?
        when(this){
            EKTEFELLE -> return OverføreOmsorgsdagerBehov.Relasjon.NåværendeEktefelle
            SAMBOER -> return OverføreOmsorgsdagerBehov.Relasjon.NåværendeSamboer
        }
    }
}
