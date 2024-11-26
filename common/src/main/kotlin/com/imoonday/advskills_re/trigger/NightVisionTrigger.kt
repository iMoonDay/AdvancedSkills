package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface NightVisionTrigger : SkillTrigger {
    
    fun hasNightVision(player: PlayerEntity): Boolean = player.isUsing()
}