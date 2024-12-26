package com.imoonday.advskills_re.util

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.minecraft.util.*

class IdentifierSerializer : KSerializer<Identifier> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Identifier = Identifier(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Identifier) = encoder.encodeString(value.toString())
}