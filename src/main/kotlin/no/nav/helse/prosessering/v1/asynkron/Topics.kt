package no.nav.helse.prosessering.v1.asynkron

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.omsorgsdageroverførningKonfigurertMapper
import no.nav.helse.prosessering.Metadata
import no.nav.helse.prosessering.v1.overforeDager.PreprossesertOverforeDagerV1
import no.nav.helse.prosessering.v1.overforeDager.SøknadOverføreDagerV1
import no.nav.k9.søknad.omsorgspenger.overføring.OmsorgspengerOverføringSøknad
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.json.JSONObject

internal data class Topic(
    val name: String,
    val serDes: SerDes
) {
    val keySerializer = StringSerializer()
    private val keySerde = Serdes.String()
    private val valueSerde = Serdes.serdeFrom(SerDes(), SerDes())
    val consumed = Consumed.with(keySerde, valueSerde)
    val produced = Produced.with(keySerde, valueSerde)
}

internal object Topics {
    val MOTTATT_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-mottatt",
        serDes = SerDes()
    )

    val PREPROSSESERT_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-preprossesert",
        serDes = SerDes()
    )


    val CLEANUP_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-cleanup",
        serDes = SerDes()
    )

    val JOURNALFORT_OVERFOREDAGER = Topic(
        name = "privat-overfore-omsorgsdager-soknad-journalfort",
        serDes = SerDes()
    )
}

data class Data(val rawJson: String)
data class CleanupOverforeDager(
    val metadata: Metadata,
    val meldingV1: PreprossesertOverforeDagerV1,
    val journalførtMelding: JournalfortOverforeDager
)

class SerDes : Serializer<TopicEntry>, Deserializer<TopicEntry> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
    override fun serialize(topic: String, entry: TopicEntry): ByteArray = entry.rawJson.toByteArray()
    override fun deserialize(topic: String, entry: ByteArray): TopicEntry = TopicEntry(String(entry))
}

internal fun TopicEntry.deserialiserTilSøknadOverføreDagerV1(): SøknadOverføreDagerV1 = omsorgsdageroverførningKonfigurertMapper().readValue(data.rawJson)
internal fun TopicEntry.deserialiserTilPreprossesertOverforeDagerV1():PreprossesertOverforeDagerV1  = omsorgsdageroverførningKonfigurertMapper().readValue(data.rawJson)
internal fun TopicEntry.deserialiserTilCleanupOverforeDager():CleanupOverforeDager  = omsorgsdageroverførningKonfigurertMapper().readValue(data.rawJson)
internal fun Any.serialiserTilData() = Data(omsorgsdageroverførningKonfigurertMapper().writeValueAsString(this))


data class JournalfortOverforeDager(val journalpostId: String, val søknad: OmsorgspengerOverføringSøknad)

data class TopicEntry(val rawJson: String) {
    constructor(metadata: Metadata, data: Data) : this(
        JSONObject(
            mapOf(
                "metadata" to JSONObject(
                    mapOf(
                        "versjon" to metadata.version,
                        "correlationId" to metadata.correlationId,
                        "requestId" to metadata.requestId
                    )
                ),
                "data" to JSONObject(data.rawJson)
            )
        ).toString()
    )

    private val entityJson = JSONObject(rawJson)
    private val metadataJson = requireNotNull(entityJson.getJSONObject("metadata"))
    private val dataJson = requireNotNull(entityJson.getJSONObject("data"))
    val metadata = Metadata(
        version = requireNotNull(metadataJson.getInt("versjon")),
        correlationId = requireNotNull(metadataJson.getString("correlationId")),
        requestId = requireNotNull(metadataJson.getString("requestId"))
    )
    val data = Data(dataJson.toString())
}
