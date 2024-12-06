package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.player.*

interface SpecialStateRenderTrigger : RenderTrigger {

    fun isInSpecialState(player: PlayerEntity): Boolean
}