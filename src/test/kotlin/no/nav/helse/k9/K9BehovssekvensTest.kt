package no.nav.helse.k9

import no.nav.helse.omsorgsdageroverførningKonfigurertMapper

class K9BehovssekvensTest {

    //TODO Lage tester som sikrer at Behovssekvensen er gyldig

    internal fun Any.somJson() = omsorgsdageroverførningKonfigurertMapper().writeValueAsString(this)

}