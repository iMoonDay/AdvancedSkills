package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.PlayerEntity

interface SaveMovingTrigger : SkillTrigger {

    fun isSaveMoving(player: PlayerEntity): Boolean = player.isUsing()
}