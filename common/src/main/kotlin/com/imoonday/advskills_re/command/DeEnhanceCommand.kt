package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object DeEnhanceCommand : PlayerCommand("de-enhance") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("skill", SkillArgumentType.validSkill())
                .then(
                    argument("enhancement", StringArgumentType.word())
                        .suggests { context, builder1 ->
                            val skill = SkillArgumentType.getSkill(context)
                            val player = getPlayer(context)
                            val enhancements = player.getEnhancements(skill)
                            CommandSource.suggestMatching(enhancements, builder1, { it.type.id }, { it.name })
                        }.executesWithPlayer(this::deEnhance)
                ).then(
                    literal("all")
                        .executesWithPlayer(this::deEnhanceAll)
                )
        ).then(
            literal("all")
                .executesWithPlayer(this::deEnhanceAllSkills)
        )

    private fun deEnhanceAllSkills(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        player.learnedSkills.forEach { player.deEnhanceAll(it) }
        context.sendFeedback("deEnhanceSkill.all", player.displayName)
        return 1
    }

    private fun deEnhanceAll(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        if (player.deEnhanceAll(skill)) {
            context.sendFeedback(
                "deEnhanceSkill.allSuccess",
                player.displayName,
                skill.name
            )
            return 1
        } else {
            context.sendFeedback(
                "deEnhanceSkill.failed",
                player.displayName,
                skill.name
            )
            return 0
        }
    }

    private fun deEnhance(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        val id = StringArgumentType.getString(context, "enhancement")
        val enhancement = player.getEnhancements(skill).find { it.type.id == id }
        if (enhancement == null) {
            val type = SkillEnhancements.get(id)
            if (type == null) {
                context.sendError("deEnhanceSkill.unknown", id)
            } else {
                context.sendFeedback("deEnhanceSkill.invalid", player.displayName, skill.name, type.name)
            }
            return 0
        }

        if (player.deEnhance(skill, enhancement.type)) {
            context.sendFeedback(
                "deEnhanceSkill.success",
                player.displayName,
                skill.name,
                enhancement.name
            )
            return 1
        } else {
            context.sendFeedback(
                "deEnhanceSkill.failed",
                player.displayName,
                skill.name
            )
            return 0
        }
    }
}