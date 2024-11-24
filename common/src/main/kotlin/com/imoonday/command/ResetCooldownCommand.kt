package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object ResetCooldownCommand : PlayerCommand("reset-cooldown") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.executesWithPlayer(::resetCooldown)

    private fun resetCooldown(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        player.learnedSkills.forEach { player.stopCooling(it) }
        context.sendFeedback(
            "resetCooldown", null,
            player.displayName.string
        )
        return 1
    }
}
