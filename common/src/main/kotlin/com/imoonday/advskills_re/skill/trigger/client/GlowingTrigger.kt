package com.imoonday.advskills_re.skill.trigger.client

import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface GlowingTrigger : SkillTrigger {

    fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean = false
}