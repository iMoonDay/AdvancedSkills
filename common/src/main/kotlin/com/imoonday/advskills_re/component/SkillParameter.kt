package com.imoonday.advskills_re.component

import kotlinx.serialization.*

@Serializable
sealed class SkillParameter<T : Number>(
    val baseValue: T,
    val enhancement: String?
) {

    abstract fun asInt(): Int

    abstract fun asFloat(): Float

    class Int(
        baseValue: kotlin.Int,
        enhancement: String?
    ) : SkillParameter<kotlin.Int>(baseValue, enhancement) {

        override fun asInt(): Int = this

        override fun asFloat(): Float = Float(baseValue.toFloat(), enhancement)
    }

    class Float(
        baseValue: kotlin.Float,
        enhancement: String?
    ) : SkillParameter<kotlin.Float>(baseValue, enhancement) {

        override fun asInt(): Int = Int(baseValue.toInt(), enhancement)

        override fun asFloat(): Float = this
    }
}