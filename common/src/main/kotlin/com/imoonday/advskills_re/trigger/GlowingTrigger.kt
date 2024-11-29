package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface GlowingTrigger : SkillTrigger {

    fun isGlowing(entity: Entity, player: PlayerEntity): Boolean = false
}