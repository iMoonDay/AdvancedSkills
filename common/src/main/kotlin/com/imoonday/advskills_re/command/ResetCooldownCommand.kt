package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object ResetCooldownCommand : PlayerCommand("reset-cooldown") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.executesWithPlayer(ResetCooldownCommand::resetCooldown)

    private fun resetCooldown(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        player.learnedSkills.forEach { player.stopCooling(it) }
        context.sendFeedback(
            "resetCooldown",
            player.displayName
        )
        return 1
    }
}
