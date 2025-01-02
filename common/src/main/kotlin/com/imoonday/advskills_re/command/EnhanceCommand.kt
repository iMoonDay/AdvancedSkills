package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object EnhanceCommand : PlayerCommand("enhance") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("skill", SkillArgumentType.validSkill())
                .then(
                    argument("enhancement", StringArgumentType.word())
                        .suggests { context, builder1 ->
                            val skill = SkillArgumentType.getSkill(context)
                            val enhancements = skill.getAvailableEnhancements()
                            CommandSource.suggestMatching(enhancements, builder1, { it.id }, { it.name })
                        }.then(
                            argument("level", IntegerArgumentType.integer(1))
                                .executesWithPlayer(this::enhance)
                        )
                ).then(
                    literal("all")
                        .executesWithPlayer(this::enhanceAll)
                )
        ).then(
            literal("all")
                .executesWithPlayer(this::enhanceAllSkills)
        )

    private fun enhanceAllSkills(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        player.learnedSkills.forEach { player.enhanceAll(it) }
        context.sendFeedback("enhanceSkill.all", player.displayName)
        return 1
    }

    private fun enhanceAll(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        val enhancements = skill.getAvailableEnhancements()
        if (player.enhanceAll(skill)) {
            context.sendFeedback(
                "enhanceSkill.allSuccess",
                enhancements.size,
                player.displayName,
                skill.name
            )
            return 1
        } else {
            context.sendFeedback(
                "enhanceSkill.failed",
                player.displayName,
                skill.name
            )
            return 0
        }
    }

    private fun enhance(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        val id = StringArgumentType.getString(context, "enhancement")
        val level = IntegerArgumentType.getInteger(context, "level")
        val enhancement = skill.getEnhancement(id)
        if (enhancement == null) {
            context.sendError("enhanceSkill.unknown", id)
            return 0
        }

        if (player.enhance(skill, enhancement.id, level)) {
            context.sendFeedback(
                "enhanceSkill.success",
                player.displayName,
                skill.name,
                level,
                enhancement.name
            )
            return 1
        } else {
            context.sendFeedback(
                "enhanceSkill.failed",
                player.displayName,
                skill.name
            )
            return 0
        }
    }
}