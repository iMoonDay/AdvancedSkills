package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.skill.enhancement.*
import net.minecraft.text.*

class SkillEnhancementType<T : SkillEnhancement>(
    val id: String,
    val name: Text,
    val description: Text,
    val maxLevel: Int,
    val factory: Factory<T>
) {

    fun interface Factory<T : SkillEnhancement> {

        fun create(type: SkillEnhancementType<T>, level: Int): T
    }
}