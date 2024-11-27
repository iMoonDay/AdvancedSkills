package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object ListCommand : PlayerCommand("list") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.executesWithPlayer(::list)

    private fun list(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
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
