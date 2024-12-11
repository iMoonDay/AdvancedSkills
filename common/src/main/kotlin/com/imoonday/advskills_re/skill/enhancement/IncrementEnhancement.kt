package com.imoonday.advskills_re.skill.enhancement

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.util.*
import net.minecraft.text.*
import kotlin.math.*

open class IncrementEnhancement(
    type: SkillEnhancementType<out IncrementEnhancement>,
    level: Int,
    var incrementPerLvl: Double
) : SkillEnhancement(type, level) {

    override val description: Text
        get() = createDescription(calculateDescriptionIncrement(incrementPerLvl).absoluteValue)

    inline fun <reified T : Number> applyIncrement(value: T): T =
        (value.toDouble() + incrementPerLvl * level).toNumber()

    open fun calculateDescriptionIncrement(increment: Double): Double = increment * level

    companion object {

        fun createFactory(incrementPerLvl: Double) = SkillEnhancementType.Factory { type, level ->
            IncrementEnhancement(type, level, incrementPerLvl)
        }
    }
}