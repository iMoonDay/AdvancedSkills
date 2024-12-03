package com.imoonday.advskills_re.trigger.renderer

import net.minecraft.entity.player.*

interface SpecialStateRenderTrigger : RendererTrigger {

    fun isInSpecialState(player: PlayerEntity): Boolean
}