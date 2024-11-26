package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*

interface GlowingTrigger : SkillTrigger {

    fun isGlowing(entity: Entity): Boolean = false
}