package com.imoonday.advskills_re.component

import kotlinx.serialization.*

@Serializable
sealed class SkillParameter {

    abstract val baseValue: Any
    abstract val enhancement: String?

    abstract fun asIntParameter(): IntParameter
    abstract fun asFloatParameter(): FloatParameter
    abstract fun asStringParameter(): StringParameter

    @Serializable
    @SerialName("IntParameter")
    data class IntParameter(
        override val baseValue: Int,
        override val enhancement: String?
    ) : SkillParameter() {

        override fun asIntParameter(): IntParameter = this
        override fun asFloatParameter(): FloatParameter = FloatParameter(baseValue.toFloat(), enhancement)
        override fun asStringParameter(): StringParameter = StringParameter(baseValue.toString(), enhancement)
    }

    @Serializable
    @SerialName("FloatParameter")
    data class FloatParameter(
        override val baseValue: Float,
        override val enhancement: String?
    ) : SkillParameter() {

        override fun asIntParameter(): IntParameter = IntParameter(baseValue.toInt(), enhancement)
        override fun asFloatParameter(): FloatParameter = this
        override fun asStringParameter(): StringParameter = StringParameter(baseValue.toString(), enhancement)
    }

    @Serializable
    @SerialName("StringParameter")
    data class StringParameter(
        override val baseValue: String,
        override val enhancement: String?
    ) : SkillParameter() {

        override fun asIntParameter(): IntParameter = IntParameter(baseValue.toIntOrNull() ?: 0, enhancement)
        override fun asFloatParameter(): FloatParameter = FloatParameter(baseValue.toFloatOrNull() ?: 0.0f, enhancement)
        override fun asStringParameter(): StringParameter = this
    }
}