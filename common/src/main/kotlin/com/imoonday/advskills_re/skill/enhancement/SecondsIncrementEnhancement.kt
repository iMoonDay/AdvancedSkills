package com.imoonday.advskills_re.skill.enhancement

import com.imoonday.advskills_re.component.*

class SecondsIncrementEnhancement(
    type: SkillEnhancementType<out SecondsIncrementEnhancement>,
    level: Int,
    incrementPerLvl: Double,
) : IncrementEnhancement(type, level, incrementPerLvl) {

    override fun calculateDescriptionIncrement(increment: Double): Double =
        super.calculateDescriptionIncrement(increment) / 20.0

    companion object {

        fun createFactory(incrementPerLvl: Double) = SkillEnhancementType.Factory { type, level ->
            SecondsIncrementEnhancement(type, level, incrementPerLvl)
        }
    }
}