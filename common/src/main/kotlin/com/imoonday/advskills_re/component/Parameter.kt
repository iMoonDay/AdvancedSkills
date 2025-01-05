package com.imoonday.advskills_re.component

import com.google.gson.*
import com.google.gson.JsonSerializer
import com.google.gson.reflect.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import net.minecraft.nbt.*
import net.minecraft.registry.*
import net.minecraft.sound.*
import net.minecraft.util.*
import java.lang.reflect.*
import java.util.*
import java.util.function.*
import kotlin.jvm.optionals.*

sealed class Parameter {

    abstract val baseValue: Any
    abstract val enhancements: List<String>

    abstract fun asInt(): IntParameter?
    abstract fun asFloat(): FloatParameter?
    abstract fun asDouble(): DoubleParameter?
    open fun asString(): StringParameter = StringParameter(baseValue.toString(), enhancements)
    abstract fun asBoolean(): BooleanParameter
    open fun asSoundEvent(): SoundEventParameter = SoundEventParameter(null, enhancements)
    open fun asList(): ListParameter = ListParameter(listOf(baseValue), enhancements)

    abstract fun toNbt(): NbtCompound
    open fun writeEnhancements(nbt: NbtCompound) {
        if (enhancements.isNotEmpty()) {
            nbt.put("enhancements", enhancements.toNbtStringList())
        }
    }

    abstract fun getType(): Type

    data class IntParameter(
        override val baseValue: Int,
        override val enhancements: List<String>
    ) : Parameter() {

        override fun asInt(): IntParameter = this
        override fun asFloat(): FloatParameter = FloatParameter(baseValue.toFloat(), enhancements)
        override fun asDouble(): DoubleParameter = DoubleParameter(baseValue.toDouble(), enhancements)
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue > 0, enhancements)
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 0)
            putInt("baseValue", baseValue)
            writeEnhancements(this)
        }

        override fun getType(): Type = Int::class.java
    }

    data class FloatParameter(
        override val baseValue: Float,
        override val enhancements: List<String>
    ) : Parameter() {

        override fun asInt(): IntParameter = IntParameter(baseValue.toInt(), enhancements)
        override fun asFloat(): FloatParameter = this
        override fun asDouble(): DoubleParameter = DoubleParameter(baseValue.toDouble(), enhancements)
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue > 0f, enhancements)
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 1)
            putFloat("baseValue", baseValue)
            writeEnhancements(this)
        }

        override fun getType(): Type = Float::class.java
    }

    data class DoubleParameter(
        override val baseValue: Double,
        override val enhancements: List<String>
    ) : Parameter() {

        override fun asInt(): IntParameter = IntParameter(baseValue.toInt(), enhancements)
        override fun asFloat(): FloatParameter = FloatParameter(baseValue.toFloat(), enhancements)
        override fun asDouble(): DoubleParameter = this
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue > 0.0, enhancements)
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 2)
            putDouble("baseValue", baseValue)
            writeEnhancements(this)
        }

        override fun getType(): Type = Double::class.java
    }

    data class BooleanParameter(
        override val baseValue: Boolean,
        override val enhancements: List<String>
    ) : Parameter() {

        override fun asInt(): IntParameter? = null
        override fun asFloat(): FloatParameter? = null
        override fun asDouble(): DoubleParameter? = null
        override fun asBoolean(): BooleanParameter = this
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 3)
            putBoolean("baseValue", baseValue)
            writeEnhancements(this)
        }

        override fun getType(): Type = Boolean::class.java
    }

    data class StringParameter(
        override val baseValue: String,
        override val enhancements: List<String>
    ) : Parameter() {

        override fun asInt(): IntParameter? = baseValue.toIntOrNull()?.let { IntParameter(it, enhancements) }
        override fun asFloat(): FloatParameter? = baseValue.toFloatOrNull()?.let { FloatParameter(it, enhancements) }
        override fun asDouble(): DoubleParameter? =
            baseValue.toDoubleOrNull()?.let { DoubleParameter(it, enhancements) }

        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue.toBoolean(), enhancements)

        override fun asString(): StringParameter = this
        override fun asSoundEvent(): SoundEventParameter =
            SoundEventParameter(Registries.SOUND_EVENT.getOrEmpty(baseValue.toIdentifier()), enhancements)

        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 4)
            putString("baseValue", baseValue)
            writeEnhancements(this)
        }

        override fun getType(): Type = String::class.java
    }

    data class SoundEventParameter(
        override val baseValue: Optional<SoundEvent>,
        override val enhancements: List<String>
    ) : Parameter() {

        constructor(baseValue: SoundEvent?, enhancements: List<String>) : this(
            Optional.ofNullable(baseValue),
            enhancements
        )

        override fun asInt(): IntParameter? = null
        override fun asFloat(): FloatParameter? = null
        override fun asDouble(): DoubleParameter? = null
        override fun asBoolean(): BooleanParameter = BooleanParameter(true, enhancements)
        override fun asString(): StringParameter = StringParameter(baseValue.orElse(null)?.id.toString(), enhancements)
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

        override fun getType(): Type = soundType

        companion object {

            @JvmStatic
            val soundType: Type = object : TypeToken<Optional<SoundEvent>>() {}.type
        }
    }

    data class ListParameter(
        override val baseValue: List<PrimitiveType>,
        override val enhancements: List<String>
    ) : Parameter() {

        constructor(baseValue: Collection<Any>, enhancements: List<String> = emptyList()) : this(
            baseValue.map(PrimitiveType.Companion::parse),
            enhancements
        )

        override fun asInt(): IntParameter = IntParameter(baseValue.size, enhancements)
        override fun asFloat(): FloatParameter = FloatParameter(baseValue.size.toFloat(), enhancements)
        override fun asDouble(): DoubleParameter = DoubleParameter(baseValue.size.toDouble(), enhancements)
        override fun asBoolean(): BooleanParameter = BooleanParameter(baseValue.isNotEmpty(), enhancements)
        override fun asString(): StringParameter =
            StringParameter(baseValue.joinToString(", ") { it.asString() }, enhancements)

        override fun asList(): ListParameter = this
        override fun toNbt(): NbtCompound = NbtCompound().apply {
            putInt("type", 6)
            put("baseValue", baseValue.toNbtList { it.toNbt() })
            writeEnhancements(this)
        }

        fun getIntList(): List<Int> = baseValue.mapNotNull { it.asInt() }
        fun getFloatList(): List<Float> = baseValue.mapNotNull { it.asFloat() }
        fun getDoubleList(): List<Double> = baseValue.mapNotNull { it.asDouble() }
        fun getBooleanList(): List<Boolean> = baseValue.mapNotNull { it.asBoolean() }
        fun getStringList(): List<String> = baseValue.map { it.asString() }

        override fun getType(): Type = List::class.java

        sealed class PrimitiveType {

            abstract fun asInt(): Int?
            abstract fun asFloat(): Float?
            abstract fun asDouble(): Double?
            abstract fun asBoolean(): Boolean?
            abstract fun asString(): String
            abstract fun toNbt(): NbtCompound

            override fun toString(): String = asString()

            data class PrimitiveInt(val value: Int) : PrimitiveType() {

                override fun asInt(): Int = value
                override fun asFloat(): Float = value.toFloat()
                override fun asDouble(): Double = value.toDouble()
                override fun asBoolean(): Boolean = value > 0
                override fun asString(): String = value.toString()
                override fun toNbt(): NbtCompound = NbtCompound().apply {
                    putInt("type", 0)
                    putInt("value", value)
                }
            }

            data class PrimitiveFloat(val value: Float) : PrimitiveType() {

                override fun asInt(): Int = value.toInt()
                override fun asFloat(): Float = value
                override fun asDouble(): Double = value.toDouble()
                override fun asBoolean(): Boolean = value > 0f
                override fun asString(): String = value.toString()
                override fun toNbt(): NbtCompound = NbtCompound().apply {
                    putInt("type", 1)
                    putFloat("value", value)
                }
            }

            data class PrimitiveDouble(val value: Double) : PrimitiveType() {

                override fun asInt(): Int = value.toInt()
                override fun asFloat(): Float = value.toFloat()
                override fun asDouble(): Double = value
                override fun asBoolean(): Boolean = value > 0.0
                override fun asString(): String = value.toString()
                override fun toNbt(): NbtCompound = NbtCompound().apply {
                    putInt("type", 2)
                    putDouble("value", value)
                }
            }

            data class PrimitiveBoolean(val value: Boolean) : PrimitiveType() {

                override fun asInt(): Int = if (value) 1 else 0
                override fun asFloat(): Float = if (value) 1f else 0f
                override fun asDouble(): Double = if (value) 1.0 else 0.0
                override fun asBoolean(): Boolean = value
                override fun asString(): String = value.toString()
                override fun toNbt(): NbtCompound = NbtCompound().apply {
                    putInt("type", 3)
                    putBoolean("value", value)
                }
            }

            data class PrimitiveString(val value: String) : PrimitiveType() {

                override fun asInt(): Int? = value.toIntOrNull()
                override fun asFloat(): Float? = value.toFloatOrNull()
                override fun asDouble(): Double? = value.toDoubleOrNull()
                override fun asBoolean(): Boolean? = value.toBooleanStrictOrNull()
                override fun asString(): String = value
                override fun toNbt(): NbtCompound = NbtCompound().apply {
                    putInt("type", 4)
                    putString("value", value)
                }
            }

            object Serializer : JsonDeserializer<PrimitiveType>, JsonSerializer<PrimitiveType> {

                override fun deserialize(
                    json: JsonElement,
                    typeOfT: Type,
                    context: JsonDeserializationContext
                ): PrimitiveType {
                    val jsonPrimitive = json.asJsonPrimitive
                    return when {
                        jsonPrimitive.isNumber -> when (jsonPrimitive.asNumber) {
                            is Int, is Long, is Short, is Byte -> PrimitiveInt(jsonPrimitive.asInt)
                            is Float -> PrimitiveFloat(jsonPrimitive.asFloat)
                            else -> PrimitiveDouble(jsonPrimitive.asDouble)
                        }

                        jsonPrimitive.isBoolean -> PrimitiveBoolean(jsonPrimitive.asBoolean)
                        else -> PrimitiveString(jsonPrimitive.asString)
                    }
                }

                override fun serialize(
                    src: PrimitiveType,
                    typeOfSrc: Type,
                    context: JsonSerializationContext
                ): JsonElement = when (src) {
                    is PrimitiveInt -> JsonPrimitive(src.value)
                    is PrimitiveFloat -> JsonPrimitive(src.value)
                    is PrimitiveDouble -> JsonPrimitive(src.value)
                    is PrimitiveBoolean -> JsonPrimitive(src.value)
                    is PrimitiveString -> JsonPrimitive(src.value)
                }
            }

            companion object {

                @JvmStatic
                fun fromNbt(nbt: NbtCompound): PrimitiveType? =
                    if (nbt.contains("type")) {
                        when (nbt.getInt("type")) {
                            0 -> PrimitiveInt(nbt.getInt("value"))
                            1 -> PrimitiveFloat(nbt.getFloat("value"))
                            2 -> PrimitiveDouble(nbt.getDouble("value"))
                            3 -> PrimitiveBoolean(nbt.getBoolean("value"))
                            4 -> PrimitiveString(nbt.getString("value"))
                            else -> fromValue(nbt)
                        }
                    } else fromValue(nbt)

                @JvmStatic
                fun fromValue(nbt: NbtCompound): PrimitiveType? =
                    when (nbt.get("value")) {
                        is NbtInt -> PrimitiveInt(nbt.getInt("value"))
                        is NbtFloat -> PrimitiveFloat(nbt.getFloat("value"))
                        is NbtDouble -> PrimitiveDouble(nbt.getDouble("value"))
                        is NbtByte -> PrimitiveBoolean(nbt.getBoolean("value"))
                        is NbtString -> PrimitiveString(nbt.getString("value"))
                        else -> null
                    }

                @JvmStatic
                fun parse(obj: Any?): PrimitiveType =
                    when (obj) {
                        is Int, is Long, is Short, is Byte -> PrimitiveInt((obj as Number).toInt())
                        is Float -> PrimitiveFloat(obj)
                        is Double -> PrimitiveDouble(obj)
                        is Boolean -> PrimitiveBoolean(obj)
                        is String -> PrimitiveString(obj)
                        is PrimitiveType -> obj
                        else -> PrimitiveString(obj.toString())
                    }
            }
        }
    }

    object Serializer : JsonDeserializer<Parameter>, JsonSerializer<Parameter> {

        private val LOGGER = LogUtils.getLogger()

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Parameter {
            val jsonObj = json.asJsonObject
            val enhancements =
                if (jsonObj.has("enhancements")) jsonObj.get(
                    "enhancements"
                ).asJsonArray.map { it.asString } else emptyList()
            val baseValue = jsonObj.get("baseValue")
            try {
                return if (baseValue.isJsonPrimitive) {
                    val primitive = baseValue.asJsonPrimitive
                    when {
                        primitive.isNumber -> {
                            primitive.asString.let {
                                create(
                                    it.toIntOrNull()
                                        ?: it.toFloatOrNull()
                                        ?: it.toDoubleOrNull()
                                        ?: primitive.asNumber,
                                    enhancements
                                )
                            }
                        }

                        primitive.isBoolean -> create(primitive.asBoolean, enhancements)
                        else -> {
                            LOGGER.warn(
                                "Invalid primitive ${primitive.asString} with type: ${primitive.javaClass.name}"
                            )
                            create(primitive.asString, enhancements)
                        }
                    }
                } else if (baseValue.isJsonArray) {
                    ListParameter(
                        baseValue.asJsonArray.map { context.deserialize(it, ListParameter.PrimitiveType::class.java) },
                        enhancements
                    )
                } else if (baseValue.isJsonObject) {
                    try {
                        SoundEventParameter(
                            context.deserialize<Optional<SoundEvent>>(
                                baseValue,
                                SoundEventParameter.soundType
                            ), enhancements
                        )
                    } catch (e: Exception) {
                        LOGGER.warn("Invalid object ${baseValue.asJsonObject} with type: ${baseValue.javaClass.name}")
                        create(baseValue.toString(), enhancements)
                    }
                } else {
                    LOGGER.warn("Unknown value $baseValue with type: ${baseValue.javaClass.name}")
                    create(baseValue.toString(), enhancements)
                }
            } catch (e: Exception) {
                LOGGER.warn("Invalid parameter: $jsonObj", e)
                return create(jsonObj.toString(), enhancements)
            }
        }

        override fun serialize(
            src: Parameter,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val json = JsonObject()
            val baseValue = src.baseValue
            val type = src.getType()
            json.add("baseValue", context.serialize(baseValue, type))
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
        fun create(baseValue: Any, enhancements: List<String> = emptyList()): Parameter = when (baseValue) {
            is Supplier<*> -> create(baseValue.get(), enhancements)
            is Number -> when (baseValue) {
                is Int, is Long, is Short, is Byte -> IntParameter(baseValue.toInt(), enhancements)
                is Float -> FloatParameter(baseValue, enhancements)
                else -> DoubleParameter(baseValue.toDouble(), enhancements)
            }

            is Boolean -> BooleanParameter(baseValue, enhancements)
            is SoundEvent -> SoundEventParameter(baseValue, enhancements)
            is StringIdentifiable -> StringParameter(baseValue.asString(), enhancements)
            is Collection<*> -> ListParameter(baseValue.map(ListParameter.PrimitiveType::parse), enhancements)
            else -> StringParameter(baseValue.toString(), enhancements)
        }

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): Parameter? {
            val type = nbt.getInt("type")
            val enhancements = nbt.getList("enhancements", NbtElement.STRING_TYPE.toInt()).map { it.asString() }
            return when (type) {
                0 -> IntParameter(nbt.getInt("baseValue"), enhancements)
                1 -> FloatParameter(nbt.getFloat("baseValue"), enhancements)
                2 -> DoubleParameter(nbt.getDouble("baseValue"), enhancements)
                3 -> BooleanParameter(nbt.getBoolean("baseValue"), enhancements)
                4 -> StringParameter(nbt.getString("baseValue"), enhancements)
                5 -> SoundEventParameter(
                    SoundEvent.CODEC.parse(NbtOps.INSTANCE, nbt.get("baseValue")).result(),
                    enhancements
                )

                6 -> ListParameter(
                    nbt.getList("baseValue", NbtElement.COMPOUND_TYPE.toInt())
                        .castToList<NbtCompound, ListParameter.PrimitiveType>(ListParameter.PrimitiveType::fromNbt),
                    enhancements
                )

                else -> null
            }
        }
    }
}