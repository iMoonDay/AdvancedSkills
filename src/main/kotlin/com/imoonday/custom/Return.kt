package com.imoonday.custom

import com.imoonday.util.UseResult
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
sealed interface Return : Task {

    override val type: String
        get() = "return"

    override fun execute(player: PlayerEntity): UseResult
}