package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object ResetDataCommand : PlayerCommand("reset") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.executesWithPlayer(::reset)

    private fun reset(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        player.resetData()
        context.sendFeedback(
            "reset",
            player.displayName
        )
        return 1
    }
}
