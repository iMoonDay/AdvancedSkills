package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object LearnCommand : PlayerCommand("learn") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.then(
            argument("skill", SkillArgumentType.skill())
                .executesWithPlayer(::learn)
        )

    private fun learn(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        if (!player.learn(skill)) {
            context.sendFeedback(
                "learnSkill", "failed",
                player.displayName.string,
                skill.name.string
            )
        }
        return 1
    }
}