package no.nav.helse.prosessering.v1.deleOmsorgsdager

import io.prometheus.client.Histogram

private val antallDeleOmsorgsdagerHistogram = Histogram.build()
    .buckets(1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,17.0,18.0,19.0,20.0)
    .name("antall_dele_omsorgsdager_histogram")
    .help("Antall omsorgsdager man deler")
    .register()

internal fun PreprossesertDeleOmsorgsdagerV1.reportMetrics() {

    if (antallDagerTilOverføre != null) {
        antallDeleOmsorgsdagerHistogram.observe(antallDagerTilOverføre.toDouble())
    }
}