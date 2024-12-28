package com.imoonday.advskills_re.component

import com.google.gson.*
import com.google.gson.JsonSerializer
import com.imoonday.advskills_re.util.*
import net.minecraft.nbt.*
import net.minecraft.registry.*
import net.minecraft.sound.*
import net.minecraft.util.*
import java.lang.reflect.*
import java.util.*
import java.util.function.*
import kotlin.jvm.optionals.*

sealed class SkillParameter {

    abstract val baseValue: Any
    abstract val enhancements: List<String>

    abstract fun asInt(): IntParameter?
    abstract fun asFloat(): FloatParameter?
    abstract fun asDouble(): DoubleParameter?
    open fun asString(): StringParameter = StringParameter(baseValue.toString(), enhancements)
    abstract fun asBoolean(): BooleanParameter
    abstract fun asSoundEvent(): SoundEventParameter?
    open fun asList(): ListParameter = ListParameter(listOf(this), enhancements)

    abstract fun toNbt(): NbtCompound
    open fun writeEnhancements(nbt: NbtCompound) {
        if (enhancements.isNotEmpty()) {
            nbt.put("enhancements", enhancements.toNbtStringList())
        }
    }

    data class IntParameter(
        override val baseValue: Int,
        override val enhancements: List<String>
    ) : SkillParameter() {

        override fun asInt(): IntParameter = this
        override fun asFloat(): FloatParameter = FloatParameter(baseValue.toFloat(), enhancements)
        override fun asDouble(): DoubleParameter = DoubleParameter(baseValue.toDouble(), enhancements)
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue > 0, enhancements)
        override fun asSoundEvent(): SoundEventParameter? = null
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 0)
            putInt("baseValue", baseValue)
            writeEnhancements(this)
        }
    }

    data class FloatParameter(
        override val baseValue: Float,
        override val enhancements: List<String>
    ) : SkillParameter() {

        override fun asInt(): IntParameter = IntParameter(baseValue.toInt(), enhancements)
        override fun asFloat(): FloatParameter = this
        override fun asDouble(): DoubleParameter = DoubleParameter(baseValue.toDouble(), enhancements)
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue > 0f, enhancements)
        override fun asSoundEvent(): SoundEventParameter? = null
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 1)
            putFloat("baseValue", baseValue)
            writeEnhancements(this)
        }
    }

    data class DoubleParameter(
        override val baseValue: Double,
        override val enhancements: List<String>
    ) : SkillParameter() {

        override fun asInt(): IntParameter = IntParameter(baseValue.toInt(), enhancements)
        override fun asFloat(): FloatParameter = FloatParameter(baseValue.toFloat(), enhancements)
        override fun asDouble(): DoubleParameter = this
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue > 0.0, enhancements)
        override fun asSoundEvent(): SoundEventParameter? = null
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 2)
            putDouble("baseValue", baseValue)
            writeEnhancements(this)
        }
    }

    data class BooleanParameter(
        override val baseValue: Boolean,
        override val enhancements: List<String>
    ) : SkillParameter() {

        override fun asInt(): IntParameter? = null
        override fun asFloat(): FloatParameter? = null
        override fun asDouble(): DoubleParameter? = null
        override fun asBoolean(): BooleanParameter = this
        override fun asSoundEvent(): SoundEventParameter? = null
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 3)
            putBoolean("baseValue", baseValue)
            writeEnhancements(this)
        }
    }

    data class StringParameter(
        override val baseValue: String,
        override val enhancements: List<String>
    ) : SkillParameter() {

        override fun asInt(): IntParameter? = baseValue.toIntOrNull()?.let { IntParameter(it, enhancements) }
        override fun asFloat(): FloatParameter? = baseValue.toFloatOrNull()?.let { FloatParameter(it, enhancements) }
        override fun asDouble(): DoubleParameter? =
            baseValue.toDoubleOrNull()?.let { DoubleParameter(it, enhancements) }

        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue.toBoolean(), enhancements)

        override fun asString(): StringParameter = this
        override fun asSoundEvent(): SoundEventParameter? =
            Registries.SOUND_EVENT.get(baseValue.toIdentifier())?.let { SoundEventParameter(it, enhancements) }

        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 4)
            putString("baseValue", baseValue)
            writeEnhancements(this)
        }
    }

    data class SoundEventParameter(
        override val baseValue: Optional<SoundEvent>,
        override val enhancements: List<String>
    ) : SkillParameter() {

        constructor(baseValue: SoundEvent?, enhancements: List<String>) : this(
            Optional.ofNullable(baseValue),
            enhancements
        )

        override fun asInt(): IntParameter? = null
        override fun asFloat(): FloatParameter? = null
        override fun asDouble(): DoubleParameter? = null
        override fun asBoolean(): BooleanParameter = BooleanParameter(true, enhancements)
        override fun asSoundEvent(): SoundEventParameter = this
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 5)
            baseValue.ifPresent {
                val tag =
                    SoundEvent.CODEC.encodeStart(NbtOps.INSTANCE, it).result().getOrNull()
                if (tag != null) {
                    put("baseValue", tag)
                }
            }
            writeEnhancements(this)
        }
    }

    data class ListParameter(
        override val baseValue: List<SkillParameter>,
        override val enhancements: List<String>
    ) : SkillParameter() {

        override fun asInt(): IntParameter? = null
        override fun asFloat(): FloatParameter? = null
        override fun asDouble(): DoubleParameter? = null
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue.isNotEmpty(), enhancements)
        override fun asSoundEvent(): SoundEventParameter? = null
        override fun asList(): ListParameter = this
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 6)
            put("baseValue", NbtList().apply {
                baseValue.forEach { add(it.toNbt()) }
            })
            writeEnhancements(this)
        }
    }

    class Serializer : JsonDeserializer<SkillParameter>, JsonSerializer<SkillParameter> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SkillParameter {
            val jsonObj = json.asJsonObject
            val enhancement =
                if (jsonObj.has("enhancement")) jsonObj.get("enhancement").asJsonArray.map { it.asString } else emptyList()
            val baseValue = jsonObj.get("baseValue")
            return if (baseValue.isJsonPrimitive) {
                val primitive = baseValue.asJsonPrimitive
                when {
                    primitive.isNumber -> create(primitive.asNumber, enhancement)
                    primitive.isBoolean -> create(primitive.asBoolean, enhancement)
                    else -> create(primitive.asString, enhancement)
                }
            } else if (baseValue.isJsonArray) {
                ListParameter(
                    baseValue.asJsonArray.map { context.deserialize(it, SkillParameter::class.java) },
                    enhancement
                )
            } else if (baseValue.isJsonObject) {
                try {
                    context.deserialize(baseValue, SoundEvent::class.java)
                } catch (e: Exception) {
                    create(baseValue.toString(), enhancement)
                }
            } else {
                create(baseValue.toString(), enhancement)
            }
        }

        override fun serialize(
            src: SkillParameter,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val json = JsonObject()
            json.add("baseValue", context.serialize(src.baseValue))
            if (src.enhancements.isNotEmpty()) {
                json.add("enhancements", JsonArray().apply {
                    src.enhancements.forEach { add(it) }
                })
            }
            return json
        }
    }

    companion object {

        @JvmStatic
        fun create(baseValue: Any, enhancements: List<String> = emptyList()): SkillParameter = when (baseValue) {
            is Supplier<*> -> create(baseValue.get(), enhancements)
            is Number -> when (baseValue) {
                is Int, is Long, is Short, is Byte -> IntParameter(baseValue.toInt(), enhancements)
                is Float -> FloatParameter(baseValue, enhancements)
                else -> DoubleParameter(baseValue.toDouble(), enhancements)
            }

            is Boolean -> BooleanParameter(baseValue, enhancements)
            is SoundEvent -> SoundEventParameter(baseValue, enhancements)
            is StringIdentifiable -> StringParameter(baseValue.asString(), enhancements)
            is List<*> -> ListParameter(baseValue.mapNotNull { it?.let { create(it, enhancements) } }, enhancements)
            else -> StringParameter(baseValue.toString(), enhancements)
        }

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): SkillParameter? {
            val type = nbt.getInt("type")
            val enhancements = nbt.getList("enhancements", NbtElement.STRING_TYPE.toInt()).map { it.asString() }
            return when (type) {
                0 -> IntParameter(nbt.getInt("baseValue"), enhancements)
                1 -> FloatParameter(nbt.getFloat("baseValue"), enhancements)
                2 -> DoubleParameter(nbt.getDouble("baseValue"), enhancements)
                3 -> BooleanParameter(nbt.getBoolean("baseValue"), enhancements)
                4 -> StringParameter(nbt.getString("baseValue"), enhancements)
                5 -> SoundEventParameter(
                    SoundEvent.CODEC.parse(NbtOps.INSTANCE, nbt.get("baseValue")).result().getOrNull(),
                    enhancements
                )

                6 -> ListParameter(
                    nbt.getList("baseValue", NbtElement.COMPOUND_TYPE.toInt()).mapNotNull { element ->
                        (element as? NbtCompound)?.let { fromNbt(it) }
                    }, enhancements
                )

                else -> null
            }
        }
    }
}