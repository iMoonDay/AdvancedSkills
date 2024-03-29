package com.imoonday.trigger

import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.entity.player.PlayerEntity

interface HeartTypeTrigger : SkillTrigger {

    fun getHeartType(player: PlayerEntity): Pair<HeartType, Int>?
}