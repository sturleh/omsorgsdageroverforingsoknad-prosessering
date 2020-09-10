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
    val dokumentUrls: List<List<URI>>,
    val språk: String?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val andreBarn: List<AndreBarn>,
    val harAleneomsorg: Boolean,
    val harAleneomsorgFor: List<Barn>,
    val harUtvidetRett: Boolean,
    val harUtvidetRettFor: List<Barn>,
    val borINorge: Boolean,
    val arbeidINorge: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val antallDagerBruktEtter1Juli: Int,
    val mottakerType: Mottaker,
    val mottakerFnr: String,
    val mottakerNavn: String,
    val antallDagerSomSkalOverføres: Int
    ) {
    internal constructor(
        melding: MeldingDeleOmsorgsdagerV1,
        søkerAktørId: AktørId,
        dokumentUrls: List<List<URI>>
    ) : this(
        språk = melding.språk,
        soknadId = melding.søknadId,
        søker = PreprossesertSøker(melding.søker, søkerAktørId),
        dokumentUrls = dokumentUrls,
        mottatt = melding.mottatt,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        andreBarn = melding.andreBarn,
        harAleneomsorg = melding.harAleneomsorg,
        harAleneomsorgFor = melding.harAleneomsorgFor,
        harUtvidetRett = melding.harUtvidetRett,
        harUtvidetRettFor = melding.harUtvidetRettFor,
        borINorge = melding.borINorge,
        arbeidINorge = melding.arbeidINorge,
        arbeidssituasjon = melding.arbeidssituasjon,
        antallDagerBruktEtter1Juli = melding.antallDagerBruktEtter1Juli,
        mottakerType = melding.mottakerType,
        mottakerFnr = melding.mottakerFnr,
        mottakerNavn = melding.mottakerNavn,
        antallDagerSomSkalOverføres = melding.antallDagerSomSkalOverføres
    )
}


