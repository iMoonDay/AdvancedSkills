package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object LearnCommand : PlayerCommand("learn") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("skill", SkillArgumentType.validSkill())
                .executesWithPlayer(::learn)
        )

    private fun learn(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        return if (!player.learn(skill)) {
            context.sendFeedback(
                "learnSkill.failed",
                player.displayName,
                skill.name
            )
            0
        } else 1
    }
}