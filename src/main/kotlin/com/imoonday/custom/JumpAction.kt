package com.imoonday.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
@SerialName("jump")
class JumpAction(
    override val data: Map<String, String> = mutableMapOf(),
) : Action {

    override fun execute(player: PlayerEntity) = player.jump()
}