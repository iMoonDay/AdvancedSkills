package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object ResetDataCommand : PlayerCommand("reset") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.executesWithPlayer(::reset)

    private fun reset(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        player.resetData()
        context.sendFeedback(
            "reset", null,
            player.displayName.string
        )
        return 1
    }
}
