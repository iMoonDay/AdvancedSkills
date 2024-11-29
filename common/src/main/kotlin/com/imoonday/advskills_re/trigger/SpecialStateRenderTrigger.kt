package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface SpecialStateRenderTrigger : RendererTrigger {

    fun isInSpecialState(player: PlayerEntity): Boolean
}