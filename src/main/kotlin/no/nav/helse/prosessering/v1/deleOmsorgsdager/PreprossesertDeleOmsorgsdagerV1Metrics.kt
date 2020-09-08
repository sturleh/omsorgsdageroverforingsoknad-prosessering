package no.nav.helse.prosessering.v1.deleOmsorgsdager

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

private val antallDeleOmsorgsdagerHistogram = Histogram.build()
    .buckets(1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,17.0,18.0,19.0,20.0)
    .name("antall_dele_omsorgsdager_histogram")
    .help("Antall omsorgsdager man deler")
    .register()

private val antallBruktedagerHistogram = Histogram.build()
    .buckets(1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,17.0,18.0,19.0,20.0)
    .name("antall_brukte_dager_histogram")
    .help("Antall omsorgsdager man allerede har brukt")
    .register()

private val jaNeiUtvidetRettCounter = Counter.build()
    .name("ja_nei_utvidet_rett_counter")
    .help("Teller for svar på ja/nei spørsmål om utvidet rett")
    .labelNames("spm", "svar")
    .register()

private val jaNeiBrukteDagerCounter = Counter.build()
    .name("ja_nei_brukte_dager_counter")
    .help("Teller for svar på ja/nei spørsmål om brukte dager")
    .labelNames("spm", "svar")
    .register()

private val jobberINorgeMenBorIkkeINorgeCounter = Counter.build()
    .name("jobber_i_norge_men_bor_ikke_i_norge_counter")
    .help("Teller for hvor mange som jobber i Norge, men ikke bor i Norge")
    .labelNames("spm", "svar")
    .register()

private val fordelingSamboerEllerEktefelleCounter = Counter.build()
    .name("samboer_eller_ektefelle_counter")
    .help("Teller hvor mange som deler med samboer eller ektefelle")
    .labelNames("spm", "svar")
    .register()

internal fun PreprossesertDeleOmsorgsdagerV1.reportMetrics() {
    antallDeleOmsorgsdagerHistogram.observe(antallDagerTilOverføre.toDouble())

    jaNeiUtvidetRettCounter.labels("utvidetRett", harUtvidetRett.tilJaEllerNei()).inc()

    fordelingSamboerEllerEktefelleCounter.labels("fordelingSamboerEllerEktefelle", overføreTilType.name).inc()

    jaNeiBrukteDagerCounter.labels("brukteDager", harDeltDagerMedAndreTidligere.tilJaEllerNei()).inc()

    if(arbeidINorge && !borINorge) jobberINorgeMenBorIkkeINorgeCounter.labels("Jobber i Norge, men bor ikke i Norge", "Ja").inc()

    if(harDeltDagerMedAndreTidligere) antallBruktedagerHistogram.observe(antallDagerHarDeltMedAndre.toDouble())
}

private fun Boolean.tilJaEllerNei(): String = if (this) "Ja" else "Nei"
