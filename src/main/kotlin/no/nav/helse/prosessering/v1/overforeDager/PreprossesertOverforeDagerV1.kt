package no.nav.helse.prosessering.v1.overforeDager

import no.nav.helse.aktoer.AktørId
import no.nav.helse.prosessering.v1.*
import java.net.URI
import java.time.ZonedDateTime

data class PreprossesertOverforeDagerV1(
    val soknadId: String,
    val mottatt: ZonedDateTime,
    val søker: PreprossesertSøker,
    val språk: String?,
    val antallDager: Int,
    val fnrMottaker: String,
    val navnMottaker: String,
    val medlemskap: Medlemskap,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val dokumentUrls: List<List<URI>>,
    val fosterbarn: List<Fosterbarn>? = listOf()
) {
    internal constructor(
        melding: SøknadOverføreDagerV1,
        søkerAktørId: AktørId,
        dokumentUrls: List<List<URI>>
    ) : this(
        språk = melding.språk,
        soknadId = melding.søknadId,
        søker = PreprossesertSøker(melding.søker, søkerAktørId),
        mottatt = melding.mottatt,
        arbeidssituasjon = melding.arbeidssituasjon,
        medlemskap = melding.medlemskap,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        antallDager = melding.antallDager,
        fnrMottaker = melding.fnrMottaker,
        navnMottaker = melding.navnMottaker,
        dokumentUrls = dokumentUrls,
        fosterbarn = melding.fosterbarn
    )
}

data class PreprossesertSøker(
    val fødselsnummer: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val aktørId: String
) {
    internal constructor(søker: Søker, aktørId: AktørId) : this(
        fødselsnummer = søker.fødselsnummer,
        fornavn = søker.fornavn,
        mellomnavn = søker.mellomnavn,
        etternavn = søker.etternavn,
        aktørId = aktørId.id
    )

    override fun toString(): String {
        return "Soker(fornavn='$fornavn', mellomnavn=$mellomnavn, etternavn='$etternavn', aktørId='$aktørId')"
    }

}

