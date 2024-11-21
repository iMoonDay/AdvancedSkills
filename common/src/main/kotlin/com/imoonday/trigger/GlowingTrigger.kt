package com.imoonday.trigger

import net.minecraft.entity.*

interface GlowingTrigger : SkillTrigger {

    fun isGlowing(entity: Entity): Boolean = false
}