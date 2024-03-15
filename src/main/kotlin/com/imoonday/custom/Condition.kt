package com.imoonday.custom

import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
sealed interface Condition : Task {

    override val type: String
        get() = "condition"
    val actions: ActionGroup
    val failedActions: ActionGroup?

    override fun execute(player: PlayerEntity): Boolean
}