package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface HungerTrigger : SkillTrigger {

    fun onFoodLevelChange(player: PlayerEntity, level: Int): Int
} 