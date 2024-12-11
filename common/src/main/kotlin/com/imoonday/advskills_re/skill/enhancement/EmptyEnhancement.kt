package com.imoonday.advskills_re.skill.enhancement

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*

class EmptyEnhancement(type: SkillEnhancementType<EmptyEnhancement>) : SkillEnhancement(type, 0) {

    companion object {

        private val instance: EmptyEnhancement by lazy { EmptyEnhancement(SkillEnhancements.EMPTY) }
        val factory = SkillEnhancementType.Factory { _, _ -> get() }
        
        fun get(): EmptyEnhancement = instance
    }
}