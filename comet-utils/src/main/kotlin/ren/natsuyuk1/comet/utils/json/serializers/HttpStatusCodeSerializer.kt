package ren.natsuyuk1.comet.utils.json.serializers

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HttpStatusCodeSerializer : KSerializer<HttpStatusCode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "ren.natsuyuk1.comet.utils.json.serializers.HttpStatusCodeSerializer",
            PrimitiveKind.INT
        )

    override fun deserialize(decoder: Decoder): HttpStatusCode = HttpStatusCode.fromValue(decoder.decodeInt())

    override fun serialize(encoder: Encoder, value: HttpStatusCode) = encoder.encodeInt(value.value)
}
