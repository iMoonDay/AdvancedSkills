package com.imoonday.advskills_re.util

import com.google.gson.*
import com.google.gson.JsonSerializer
import com.mojang.logging.*
import com.mojang.serialization.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.lang.reflect.*
import java.util.*
import kotlin.jvm.optionals.*

object Serializers {

    private val LOGGER = LogUtils.getLogger()

    @JvmStatic
    val SOUND_EVENT = CodecSerializer(SoundEvent.CODEC)

    class CodecSerializer<T>(val codec: Codec<T>) : JsonSerializer<T>, JsonDeserializer<T> {

        override fun serialize(value: T, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val optional = codec.encodeStart(JsonOps.INSTANCE, value).resultOrPartial(::sendError)
            return optional.getOrDefault(JsonNull.INSTANCE)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T {
            val optional = codec.parse(JsonOps.INSTANCE, json).resultOrPartial(::throwError)
            return optional.get()
        }

        private fun sendError(it: String?) {
            LOGGER.error("Failed to encode with {}: {}", this.javaClass.simpleName, it)
        }

        @Throws(JsonIOException::class)
        private fun throwError(it: String?) {
            throw JsonIOException("Failed to decode with ${this.javaClass.simpleName}: $it")
        }
    }

    object TextSerializer : JsonDeserializer<MutableText>, JsonSerializer<Text> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): MutableText = Text.Serializer.fromJson(json) ?: Text.empty()

        override fun serialize(src: Text, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            Text.Serializer.toJsonTree(src)
    }

    class OptionalSerializer<T : Any>(private val clazz: Class<T>) : JsonSerializer<Optional<T>>,
        JsonDeserializer<Optional<T>> {

        override fun serialize(src: Optional<T>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            if (src.isPresent) context.serialize(src.get()) else JsonNull.INSTANCE

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Optional<T> = if (json.isJsonNull) Optional.empty<T>()
        else Optional.ofNullable(context.deserialize<T>(json, clazz))
    }
}