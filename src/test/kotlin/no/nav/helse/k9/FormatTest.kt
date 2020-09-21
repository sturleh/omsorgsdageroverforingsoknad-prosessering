package no.nav.helse.k9

import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.k9.søknad.omsorgspenger.overføring.OmsorgspengerOverføringSøknad
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal fun String.assertOverføreDagerFormat() {
    val rawJson = JSONObject(this)

    val metadata = assertNotNull(rawJson.getJSONObject("metadata"))
    assertNotNull(metadata.getString("correlationId"))

    val data = assertNotNull(rawJson.getJSONObject("data"))

    assertNotNull(data.getString("journalpostId"))
    val søknad = assertNotNull(data.getJSONObject("søknad"))

    val rekonstruertSøknad = OmsorgspengerOverføringSøknad
        .builder()
        .json(søknad.toString())
        .build()

    JSONAssert.assertEquals(søknad.toString(), OmsorgspengerOverføringSøknad.SerDes.serialize(rekonstruertSøknad), true)
}

internal fun String.assertK9RapidFormat(id: String) {
    val rawJson = JSONObject(this)

    assertTrue(rawJson.getJSONArray("@behovsrekkefølge").getString(0) == "OverføreOmsorgsdager")
    assertTrue(rawJson.getString("@type") == "Behovssekvens")
    assertTrue(rawJson.getString("@id") == id)

    assertNotNull(rawJson.getString("@correlationId"))
    assertNotNull(rawJson.getJSONObject("@behov"))

    Behovssekvens
}
