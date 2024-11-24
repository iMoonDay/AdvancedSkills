package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object ListCommand : PlayerCommand("list") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.executesWithPlayer(::list)

    private fun list(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        val skills = player.learnedSkills

        if (skills.isNotEmpty()) {
            context.sendMessage(
                skills.joinToString(", ") { it.name.string }.toText()
            )
        }
        return skills.size
    }
}
