package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface CooldownTrigger : SkillTrigger {

    fun getCooldown(player: PlayerEntity, original: Int): Int = original
}