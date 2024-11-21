package com.imoonday.trigger

import net.minecraft.client.gui.hud.InGameHud.*
import net.minecraft.entity.player.*

interface HeartTypeTrigger : SkillTrigger {

    fun getHeartType(player: PlayerEntity): Pair<HeartType, Int>?
}