package no.nav.helse.prosessering.v1.asynkron

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.prosessering.Metadata
import no.nav.helse.prosessering.v1.overforeDager.PreprossesertOverforeDagerV1
import no.nav.helse.prosessering.v1.overforeDager.SøknadOverføreDagerV1
import no.nav.k9.søknad.omsorgspenger.OmsorgspengerSøknad
import no.nav.k9.søknad.omsorgspenger.overføring.OmsorgspengerOverføringSøknad
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

data class TopicEntry<V>(val metadata: Metadata, val data: V)

data class CleanupOverforeDager(val metadata: Metadata, val meldingV1: PreprossesertOverforeDagerV1, val journalførtMelding: JournalfortOverforeDager)

data class JournalfortOverforeDager(val journalpostId: String, val søknad: OmsorgspengerOverføringSøknad)

internal data class Topic<V>(
    val name: String,
    val serDes : SerDes<V>
) {
    val keySerializer = StringSerializer()
    val keySerde = Serdes.String()
    val valueSerde = Serdes.serdeFrom(serDes, serDes)
}

internal object Topics {
    val MOTTATT_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-mottatt",
        serDes = MottattSoknadSerDesOverforeDager()
    )
    val PREPROSSESERT_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-preprossesert",
        serDes = PreprossesertSerDesOverforeDager()
    )
    val CLEANUP_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-cleanup",
        serDes = CleanupSerDesOverforeDager()
    )
    val JOURNALFORT_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-journalfort",
        serDes = JournalfortSerDesOverforeDager()
    )
}

internal abstract class SerDes<V> : Serializer<V>, Deserializer<V> {
    protected val objectMapper = jacksonObjectMapper()
        .dusseldorfConfigured()
        .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    override fun serialize(topic: String?, data: V): ByteArray? {
        return data?.let {
            objectMapper.writeValueAsBytes(it)
        }
    }
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}

private class MottattSoknadSerDesOverforeDager: SerDes<TopicEntry<SøknadOverføreDagerV1>>() {
    override fun deserialize(topic: String?, data: ByteArray?): TopicEntry<SøknadOverføreDagerV1>? {
        return data?.let {
            objectMapper.readValue<TopicEntry<SøknadOverføreDagerV1>>(it)
        }
    }
}

private class PreprossesertSerDesOverforeDager: SerDes<TopicEntry<PreprossesertOverforeDagerV1>>() {
    override fun deserialize(topic: String?, data: ByteArray?): TopicEntry<PreprossesertOverforeDagerV1>? {
        return data?.let {
            objectMapper.readValue(it)
        }
    }
}

private class CleanupSerDesOverforeDager: SerDes<TopicEntry<CleanupOverforeDager>>() {
    override fun deserialize(topic: String?, data: ByteArray?): TopicEntry<CleanupOverforeDager>? {
        return data?.let {
            objectMapper.readValue(it)
        }
    }
}

private class JournalfortSerDesOverforeDager: SerDes<TopicEntry<JournalfortOverforeDager>>() {
    override fun deserialize(topic: String?, data: ByteArray?): TopicEntry<JournalfortOverforeDager>? {
        return data?.let {
            objectMapper.readValue(it)
        }
    }
}