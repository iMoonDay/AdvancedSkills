package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.player.*

interface DisguiseTrigger : RenderTrigger {

    fun isDisguising(player: PlayerEntity): Boolean = player.isUsing()
}