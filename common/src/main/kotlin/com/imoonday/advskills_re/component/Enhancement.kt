package com.imoonday.advskills_re.component

import com.google.gson.*
import com.google.gson.annotations.*
import net.minecraft.nbt.*
import net.minecraft.text.*
import java.lang.reflect.*
import kotlin.math.*

sealed class Enhancement {

    abstract val id: String
    abstract val name: Text
    abstract val description: String
    abstract val maxLevel: Int
    abstract val operation: Operation

    open fun getValue(level: Int): Float = 0.0f

    open fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue

    fun getEnhancedValue(level: Int, baseValue: Float): Float =
        getEnhancedValue(level, baseValue.toDouble()).toFloat()

    fun getEnhancedValue(level: Int, baseValue: Int): Int =
        getEnhancedValue(level, baseValue.toDouble()).toInt()

    open fun toNbt(): NbtCompound = NbtCompound().apply {
        putString("id", id)
        putString("name", Text.Serializer.toJson(name))
        putString("description", description)
        putInt("maxLevel", maxLevel)
        putInt("operation", operation.ordinal)
    }

    class LevelLess(
        override val id: String,
        override val name: Text,
        override val description: String
    ) : Enhancement() {

        override val maxLevel: Int = 1
        override val operation: Operation = Operation.NONE
    }

    abstract class Leveled : Enhancement() {

        abstract val valuePerLvl: Float

        override fun getValue(level: Int): Float = valuePerLvl * level

        override fun toNbt(): NbtCompound = super.toNbt().apply {
            putFloat("valuePerLvl", valuePerLvl)
        }
    }

    class Increment(
        override val id: String,
        override val name: Text,
        override val description: String,
        override val valuePerLvl: Float,
        override val maxLevel: Int
    ) : Leveled() {

        override val operation: Operation = Operation.ADDITION

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue + getValue(level)
    }

    class Multiply(
        override val id: String,
        override val name: Text,
        override val description: String,
        override val valuePerLvl: Float,
        override val maxLevel: Int
    ) : Leveled() {

        override val operation: Operation = Operation.MULTIPLY

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue * (1f + getValue(level))
    }

    enum class Operation {

        @SerializedName("none")
        NONE,

        @SerializedName("addition")
        ADDITION,

        @SerializedName("multiply")
        MULTIPLY
    }

    class Serializer : JsonDeserializer<Enhancement>, JsonSerializer<Enhancement> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Enhancement {
            val obj = json.asJsonObject
            val id = obj.get("id").asString
            val name = context.deserialize<Text>(obj.get("name"), Text::class.java)
            val description = obj.get("description").asString
            return when (val operation = context.deserialize<Operation>(obj.get("operation"), Operation::class.java)) {
                Operation.NONE -> LevelLess(id, name, description)
                Operation.ADDITION -> {
                    val value = obj.get("valuePerLvl").asFloat
                    val maxLevel = obj.get("maxLevel").asInt
                    Increment(id, name, description, value, maxLevel)
                }

                Operation.MULTIPLY -> {
                    val value = obj.get("valuePerLvl").asFloat
                    val maxLevel = obj.get("maxLevel").asInt
                    Multiply(id, name, description, value, maxLevel)
                }

                else -> throw IllegalArgumentException("Invalid operation: $operation")
            }
        }

        override fun serialize(src: Enhancement, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            JsonObject().apply {
                addProperty("id", src.id)
                add("name", context.serialize(src.name))
                addProperty("description", src.description)
                if (src is Leveled) {
                    addProperty("valuePerLvl", src.valuePerLvl)
                }
                if (src !is LevelLess) {
                    addProperty("maxLevel", src.maxLevel)
                }
                add("operation", context.serialize(src.operation))
            }
    }

    enum class ArgFormatters : (Float) -> String {
        SELF {

            override fun format(value: Float): Any {
                val formatted = String.format("%.2f", value)
                return formatted.replace(Regex("0+$"), "").replace(Regex("\\.$"), "")
            }
        },
        INT {

            override fun format(value: Float): Any = value.roundToInt()
        },
        INT_PERCENT {

            override fun format(value: Float): Any = "${(value * 100).roundToInt()}%"
        },
        FLOAT_PERCENT {

            override fun format(value: Float): Any = "${value * 100f}%"
        };

        abstract fun format(value: Float): Any

        override fun invoke(value: Float): String = "${if (value < 0) "-" else "+"}${format(value.absoluteValue)}"
    }

    companion object {

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): Enhancement? {
            val id = nbt.getString("id")
            val name = Text.Serializer.fromJson(nbt.getString("name")) ?: Text.literal("???")
            val description = nbt.getString("description")
            val maxLevel = nbt.getInt("maxLevel")
            val operation = Operation.entries.getOrElse(nbt.getInt("operation")) { return null }
            return when (operation) {
                Operation.NONE -> LevelLess(id, name, description)
                Operation.ADDITION -> {
                    val valuePerLvl = nbt.getFloat("valuePerLvl")
                    Increment(id, name, description, valuePerLvl, maxLevel)
                }

                Operation.MULTIPLY -> {
                    val valuePerLvl = nbt.getFloat("valuePerLvl")
                    Multiply(id, name, description, valuePerLvl, maxLevel)
                }
            }
        }
    }
}