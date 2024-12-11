package com.imoonday.advskills_re.skill.enhancement

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.util.*
import net.minecraft.text.*
import kotlin.math.*

class FixedValueEnhancement(
    type: SkillEnhancementType<out FixedValueEnhancement>,
    level: Int,
    var multiplier: Double
) : SkillEnhancement(type, level) {

    override val description: Text
        get() = createDescription((multiplier * level * 100).toInt().absoluteValue)

    inline fun <reified T : Number> applyMultiplier(value: T): T =
        (value.toDouble() * (1 + multiplier * level)).toNumber()

    companion object {

        fun createFactory(total: Double): SkillEnhancementType.Factory<FixedValueEnhancement> =
            SkillEnhancementType.Factory { type, level -> FixedValueEnhancement(type, level, total / type.maxLevel) }
    }
}