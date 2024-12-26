package com.imoonday.advskills_re.component

import kotlinx.serialization.*
import net.minecraft.text.Text

@Serializable
sealed class Enhancement(
    val id: String,
    val name: Text,
    val description: String,
    val maxLevel: Int,
    val type: Type
) {

    open fun getValue(level: Int): Float = 0.0f

    open fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue

    fun getEnhancedValue(level: Int, baseValue: Float): Float =
        getEnhancedValue(level, baseValue.toDouble()).toFloat()

    fun getEnhancedValue(level: Int, baseValue: Int): Int =
        getEnhancedValue(level, baseValue.toDouble()).toInt()

    class LevelLess(
        id: String,
        name: Text,
        description: String
    ) : Enhancement(id, name, description, 1, Type.LEVEL_LESS)

    abstract class Leveled(
        id: String,
        name: Text,
        description: String,
        val value: Float,
        maxLevel: Int,
        type: Type
    ) : Enhancement(id, name, description, maxLevel, type)

    class Increment(
        id: String,
        name: Text,
        description: String,
        value: Float,
        maxLevel: Int
    ) : Leveled(id, name, description, value, maxLevel, Type.ADDITION) {

        override fun getValue(level: Int): Float = value * level

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue + getValue(level)
    }

    class Multiply(
        id: String,
        name: Text,
        description: String,
        value: Float,
        maxLevel: Int
    ) : Leveled(id, name, description, value, maxLevel, Type.MULTIPLY) {

        override fun getValue(level: Int): Float = (1f + value * level)

        override fun getEnhancedValue(level: Int, baseValue: Double): Double = baseValue * getValue(level)
    }

    enum class Type {
        LEVEL_LESS, ADDITION, MULTIPLY
    }
}