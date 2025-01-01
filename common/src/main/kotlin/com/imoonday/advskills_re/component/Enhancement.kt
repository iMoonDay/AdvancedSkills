package com.imoonday.advskills_re.component

import com.google.gson.*
import com.google.gson.annotations.*
import com.google.gson.reflect.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import net.minecraft.nbt.*
import net.minecraft.text.*
import java.lang.reflect.*
import javax.script.*
import kotlin.math.*

sealed class Enhancement {

    abstract val id: String
    abstract val name: Text
    abstract val description: String
    abstract val maxLevel: Int
    abstract val operation: Operation
    abstract val weight: Weight

    open fun getValue(level: Int): Double = 0.0

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
        put("weight", weight.toNbt())
    }

    class LevelLess(
        override val id: String,
        override val name: Text,
        override val description: String,
        override val weight: Weight
    ) : Enhancement() {

        override val maxLevel: Int = 1
        override val operation: Operation = Operation.NONE
    }

    abstract class Leveled : Enhancement() {

        abstract val valuePerLvl: Double

        override fun getValue(level: Int): Double = valuePerLvl * level

        override fun toNbt(): NbtCompound = super.toNbt().apply {
            putDouble("valuePerLvl", valuePerLvl)
        }
    }

    class Increment(
        override val id: String,
        override val name: Text,
        override val description: String,
        override val valuePerLvl: Double,
        override val maxLevel: Int,
        override val weight: Weight
    ) : Leveled() {

        override val operation: Operation = Operation.ADDITION

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue + getValue(level)
    }

    class MultiplyBase(
        override val id: String,
        override val name: Text,
        override val description: String,
        override val valuePerLvl: Double,
        override val maxLevel: Int,
        override val weight: Weight
    ) : Leveled() {

        override val operation: Operation = Operation.MULTIPLY_BASE

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue * (1.0 + getValue(level))
    }

    class MultiplyTotal(
        override val id: String,
        override val name: Text,
        override val description: String,
        override val valuePerLvl: Double,
        override val maxLevel: Int,
        override val weight: Weight
    ) : Leveled() {

        override val operation: Operation = Operation.MULTIPLY_TOTAL

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue * (1.0 + getValue(level))
    }

    enum class Operation {

        @SerializedName("none")
        NONE,

        @SerializedName("addition")
        ADDITION,

        @SerializedName("multiply_base")
        MULTIPLY_BASE,

        @SerializedName("multiply_total")
        MULTIPLY_TOTAL
    }

    object Serializer : JsonDeserializer<Enhancement>, JsonSerializer<Enhancement> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Enhancement {
            val obj = json.asJsonObject
            val id = obj.get("id").asString
            val name = context.deserialize<Text>(obj.get("name"), Text::class.java)
            val description = obj.get("description").asString
            val weight = context.deserialize<Weight>(obj.get("weight"), Weight::class.java)
            val operation = try {
                context.deserialize(obj.get("operation"), Operation::class.java)
            } catch (e: Exception) {
                LOGGER.error("Failed to deserialize operation: $json", e)
                Operation.NONE
            }
            return when (operation) {
                Operation.NONE -> LevelLess(id, name, description, weight)
                Operation.ADDITION -> {
                    val value = obj.get("valuePerLvl").asDouble
                    val maxLevel = obj.get("maxLevel").asInt
                    Increment(id, name, description, value, maxLevel, weight)
                }

                Operation.MULTIPLY_BASE -> {
                    val value = obj.get("valuePerLvl").asDouble
                    val maxLevel = obj.get("maxLevel").asInt
                    MultiplyBase(id, name, description, value, maxLevel, weight)
                }

                Operation.MULTIPLY_TOTAL -> {
                    val value = obj.get("valuePerLvl").asDouble
                    val maxLevel = obj.get("maxLevel").asInt
                    MultiplyTotal(id, name, description, value, maxLevel, weight)
                }
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
                add("weight", context.serialize(src.weight))
            }
    }

    enum class ArgFormatters : (Double) -> String {
        SELF {

            override fun format(value: Double): Any {
                val formatted = String.format("%.2f", value)
                return formatted.replace(Regex("0+$"), "").replace(Regex("\\.$"), "")
            }
        },
        INT {

            override fun format(value: Double): Any = value.roundToInt()
        },
        INT_PERCENT {

            override fun format(value: Double): Any = "${(value * 100).roundToInt()}%"
        },
        FLOAT_PERCENT {

            override fun format(value: Double): Any = "${value * 100}%"
        };

        abstract fun format(value: Double): Any

        override fun invoke(value: Double): String = "${if (value < 0) "-" else "+"}${format(value.absoluteValue)}"
    }

    sealed class Weight {
        data class Fixed(val weight: Int) : Weight() {

            override fun getWeight(level: Int): Int = weight
            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 0)
                putInt("weight", weight)
            }
        }

        data class Random(val range: IntRange) : Weight() {

            override fun getWeight(level: Int): Int = range.random()
            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 1)
                putInt("min", range.first)
                putInt("max", range.last)
            }
        }

        data class Multiplier(val multiplier: Double) : Weight() {

            override fun getWeight(level: Int): Int = (multiplier * level).roundToInt()
            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 2)
                putDouble("multiplier", multiplier)
            }
        }

        data class Map(val map: kotlin.collections.Map<Int, Int>, val default: Int) : Weight() {

            override fun getWeight(level: Int): Int = map.getOrDefault(level, default)
            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 3)
                put("map", map.toNbtCompound { k, v -> putInt(k.toString(), v) })
                putInt("default", default)
            }
        }

        data class Expression(val expression: String) : Weight() {

            override fun getWeight(level: Int): Int = eval(expression.replace("{level}", level.toString()))

            private fun eval(expression: String): Int = try {
                val engine = ScriptEngineManager().getEngineByName("JavaScript")
                (engine.eval(expression) as Double).roundToInt()
            } catch (e: Exception) {
                0
            }

            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 4)
                putString("expression", expression)
            }
        }

        data class File(val filePath: String) : Weight() {

            override fun getWeight(level: Int): Int = try {
                java.io.File(filePath.replace("{level}", level.toString())).readText().trim().toInt()
            } catch (e: Exception) {
                0
            }

            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 5)
                putString("filePath", filePath)
            }
        }

        data class Incremental(val start: Int, val increment: Int) : Weight() {

            override fun getWeight(level: Int): Int = start + (level - 1) * increment
            override fun toNbt(): NbtCompound = NbtCompound().apply {
                putInt("type", 6)
                putInt("start", start)
                putInt("increment", increment)
            }
        }

        abstract fun getWeight(level: Int): Int
        abstract fun toNbt(): NbtCompound

        object Serializer : JsonDeserializer<Weight>, JsonSerializer<Weight> {

            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Weight {
                try {
                    when {
                        json.isJsonPrimitive -> return Fixed(json.asInt)
                        json.isJsonObject -> {
                            val obj = json.asJsonObject
                            when {
                                obj.has("min") && obj.has("max") ->
                                    return Random(IntRange(obj.get("min").asInt, obj.get("max").asInt))

                                obj.has("multiplier") -> return Multiplier(obj.get("multiplier").asDouble)
                                obj.has("map") -> {
                                    val map = context.deserialize<kotlin.collections.Map<Int, Int>>(
                                        obj.get("map"),
                                        object : TypeToken<kotlin.collections.Map<Int, Int>>() {}.type
                                    )
                                    val default = obj.get("default").asInt
                                    return Map(map, default)
                                }

                                obj.has("expression") -> return Expression(obj.get("expression").asString)
                                obj.has("filePath") -> return File(obj.get("filePath").asString)
                                obj.has("start") && obj.has("increment") ->
                                    return Incremental(obj.get("start").asInt, obj.get("increment").asInt)
                            }
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to deserialize weight: $json", e)
                }
                return Fixed(0)
            }

            override fun serialize(src: Weight, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                return when (src) {
                    is Fixed -> JsonPrimitive(src.weight)

                    is Random -> JsonObject().apply {
                        addProperty("min", src.range.first)
                        addProperty("max", src.range.last)
                    }

                    is Multiplier -> JsonObject().apply {
                        addProperty("multiplier", src.multiplier)
                    }

                    is Map -> JsonObject().apply {
                        add("map", context.serialize(src.map))
                        addProperty("default", src.default)
                    }

                    is Expression -> JsonObject().apply {
                        addProperty("expression", src.expression)
                    }

                    is File -> JsonObject().apply {
                        addProperty("filePath", src.filePath)
                    }

                    is Incremental -> JsonObject().apply {
                        addProperty("start", src.start)
                        addProperty("increment", src.increment)
                    }
                }
            }
        }

        companion object {

            private val LOGGER = LogUtils.getLogger()

            @JvmStatic
            fun fromNbt(nbt: NbtCompound): Weight? = when (nbt.getInt("type")) {
                0 -> Fixed(nbt.getInt("weight"))
                1 -> Random(IntRange(nbt.getInt("min"), nbt.getInt("max")))
                2 -> Multiplier(nbt.getDouble("multiplier"))
                3 -> Map(
                    nbt.getCompound("map").toStringMap { getInt(it) }
                        .mapNotNull { (it.key.toIntOrNull() ?: return@mapNotNull null) to it.value }
                        .toMap(),
                    nbt.getInt("default")
                )

                4 -> Expression(nbt.getString("expression"))
                5 -> File(nbt.getString("filePath"))
                6 -> Incremental(nbt.getInt("start"), nbt.getInt("increment"))
                else -> null
            }
        }
    }

    companion object {

        private val LOGGER = LogUtils.getLogger()

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): Enhancement? {
            val id = nbt.getString("id")
            val name = Text.Serializer.fromJson(nbt.getString("name")) ?: Text.literal("???")
            val description = nbt.getString("description")
            val maxLevel = nbt.getInt("maxLevel")
            val operation = Operation.entries.getOrElse(nbt.getInt("operation")) { return null }
            val weight = Weight.fromNbt(nbt.getCompound("weight")) ?: Weight.Fixed(0)
            return when (operation) {
                Operation.NONE -> LevelLess(id, name, description, weight)
                Operation.ADDITION -> {
                    val valuePerLvl = nbt.getDouble("valuePerLvl")
                    Increment(id, name, description, valuePerLvl, maxLevel, weight)
                }

                Operation.MULTIPLY_BASE -> {
                    val valuePerLvl = nbt.getDouble("valuePerLvl")
                    MultiplyBase(id, name, description, valuePerLvl, maxLevel, weight)
                }

                Operation.MULTIPLY_TOTAL -> {
                    val valuePerLvl = nbt.getDouble("valuePerLvl")
                    MultiplyTotal(id, name, description, valuePerLvl, maxLevel, weight)
                }
            }
        }

        @JvmStatic
        inline fun <reified T : Number> calculateValue(
            enhancements: Map<Enhancement, EnhancementData>,
            baseValue: T
        ): T {
            val addition = enhancements.filter { it.key.operation == Operation.ADDITION && it.value.activated }
                .map { it.key.getValue(it.value.currentLevel) }
            val multiplyBase = enhancements.filter { it.key.operation == Operation.MULTIPLY_BASE && it.value.activated }
                .map { it.key.getValue(it.value.currentLevel) }
            val multiplyTotal =
                enhancements.filter { it.key.operation == Operation.MULTIPLY_TOTAL && it.value.activated }
                    .map { it.key.getValue(it.value.currentLevel) }

            var value = baseValue.toDouble()
            value += addition.sum()
            var result = value
            multiplyBase.forEach { result += value * it }
            multiplyTotal.forEach { result *= 1.0 + it }
            return result.toNumber()
        }
    }
}