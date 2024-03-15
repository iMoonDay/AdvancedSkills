package com.imoonday.custom

import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
sealed interface Action : Task {

    override val type: String
        get() = "action"

    override fun execute(player: PlayerEntity)
}