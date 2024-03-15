package com.imoonday.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
@SerialName("trigger")
class TriggerImpl(override val trigger: String) : Trigger {

    override val data: Map<String, String> = emptyMap()

    override fun execute(player: PlayerEntity): Any = Unit
}