package com.imoonday.custom

import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
data class ActionGroup(
    val actions: List<Action>,
) {

    fun run(player: PlayerEntity) = actions.forEach { it.execute(player) }
}
