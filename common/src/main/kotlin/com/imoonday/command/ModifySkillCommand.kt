package com.imoonday.command

import com.imoonday.config.*
import com.imoonday.skill.*
import com.imoonday.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import com.mojang.brigadier.suggestion.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import java.util.concurrent.*

object ModifySkillCommand : BaseCommand("modify") {

    override fun build(): ArgumentBuilder<ServerCommandSource, *> =
        argument("skill", SkillArgumentType.skill())
            .then(
                literal("cooldown")
                    .then(
                        literal("set")
                            .then(
                                argument("duration", IntegerArgumentType.integer(0))
                                    .executes(::setCooldown)
                            )
                    )
                    .then(literal("reset").executes(::resetCooldown))
            )
            .then(
                literal("rarity")
                    .then(
                        literal("set")
                            .then(
                                argument("rarity", StringArgumentType.word())
                                    .suggests { _, builder -> suggestRarity(builder) }
                                    .executes(::setRarity)
                            )
                    )
                    .then(literal("reset").executes(::resetRarity))
            )
            .then(
                literal("time")
                    .then(
                        literal("set")
                            .then(
                                argument("time", IntegerArgumentType.integer(0))
                                    .executes(::setTime)
                            )
                    )
                    .then(literal("reset").executes(::resetTime))
            )

    private fun setTime(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val time = IntegerArgumentType.getInteger(context, "time")
        SkillConfig.instance.skillModifier
            .computeIfAbsent(skill.id.namespace) { mutableMapOf() }
            .computeIfAbsent(skill.id.path) { SkillModifier.EMPTY }
            .time = time
        SkillConfig.save()
        context.syncConfig()
        context.sendFeedback(
            "time", "set",
            skill.name,
            time
        )
        return 1
    }

    private fun resetTime(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        SkillConfig.instance.skillModifier
            .computeIfAbsent(skill.id.namespace) { mutableMapOf() }
            .computeIfAbsent(skill.id.path) { SkillModifier.EMPTY }
            .time = null
        SkillConfig.save()
        context.syncConfig()
        context.sendFeedback(
            "time", "reset",
            skill.name
        )
        return 1
    }

    private fun resetRarity(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val id = skill.id
        SkillConfig.instance.skillModifier
            .computeIfAbsent(id.namespace) { mutableMapOf() }
            .computeIfAbsent(id.path) { SkillModifier.EMPTY }
            .rarity = null
        SkillConfig.save()
        context.syncConfig()
        context.sendFeedback(
            "rarity", "reset",
            skill.name
        )
        return 1
    }

    private fun setRarity(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val rarityStr = StringArgumentType.getString(context, "rarity")
        val rarity = Skill.Rarity.fromId(rarityStr)
        return if (rarity == null) {
            context.sendError(
                "rarity", "invalid",
                rarityStr
            )
            0
        } else {
            SkillConfig.instance.skillModifier
                .computeIfAbsent(skill.id.namespace) { mutableMapOf() }
                .computeIfAbsent(skill.id.path) { SkillModifier.EMPTY }
                .rarity = rarity
            SkillConfig.save()
            context.syncConfig()
            context.sendFeedback(
                "rarity", "set",
                skill.name,
                rarity.displayName
            )
            1
        }
    }

    private fun suggestRarity(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        CommandSource.suggestMatching(
            Skill.Rarity.entries,
            builder,
            Skill.Rarity::id,
            Skill.Rarity::displayName
        )

    private fun resetCooldown(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        SkillConfig.instance.skillModifier
            .computeIfAbsent(skill.id.namespace) { mutableMapOf() }
            .computeIfAbsent(skill.id.path) { SkillModifier.EMPTY }
            .cooldown = null
        SkillConfig.save()
        context.syncConfig()
        context.sendFeedback(
            "cooldown", "reset",
            skill.name
        )
        return 1
    }

    private fun setCooldown(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val duration = IntegerArgumentType.getInteger(context, "duration") * 20
        SkillConfig.instance.skillModifier
            .computeIfAbsent(skill.id.namespace) { mutableMapOf() }
            .computeIfAbsent(skill.id.path) { SkillModifier.EMPTY }
            .cooldown = duration
        SkillConfig.save()
        context.syncConfig()
        context.sendFeedback(
            "cooldown", "set",
            skill.name,
            duration
        )
        return 1
    }

    private fun CommandContext<ServerCommandSource>.getSkill() =
        SkillArgumentType.getSkill(this)
}