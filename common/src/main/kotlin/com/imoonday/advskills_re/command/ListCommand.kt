package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*
import net.minecraft.text.*

object ListCommand : PlayerCommand("list") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.executesWithPlayer(::list)

    private fun list(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skills = player.learnedSkills

        val size = skills.size
        if (skills.isNotEmpty()) {
            var text = Text.empty()
            skills.forEachIndexed { i, skill ->
                text = text.append(skill.getNameWithHoverEvent(context.source.world))
                if (i != size - 1) {
                    text = text.append(", ")
                }
            }
            context.sendMessage(text)
        }
        return size
    }
}
