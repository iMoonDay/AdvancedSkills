package com.imoonday.trigger

import net.minecraft.client.gui.*
import net.minecraft.entity.player.*

interface SpecialStateRenderTrigger : SkillTrigger {

    fun isInSpecialState(player: PlayerEntity): Boolean

    fun renderSpecialState(context: DrawContext) = Unit
}