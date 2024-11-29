package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import com.mojang.brigadier.suggestion.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import java.util.concurrent.*

object ModifySkillCommand : BaseCommand("modify") {

    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("skill", SkillArgumentType.validSkill())
                .then(
                    literal("cooldown")
                        .then(
                            literal("set")
                                .then(
                                    argument("seconds", IntegerArgumentType.integer(0))
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
                                        .suggests { _, builder1 -> suggestRarity(builder1) }
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
                                    argument("seconds", IntegerArgumentType.integer(0))
                                        .executes(::setTime)
                                )
                        )
                        .then(literal("reset").executes(::resetTime))
                )
        )

    private fun setTime(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val seconds = IntegerArgumentType.getInteger(context, "seconds")
        context.skillConfig.getOrCreateModifier(skill.id).time = seconds * 20
        context.syncConfig()
        context.sendFeedback(
            "time.set",
            skill.name,
            seconds
        )
        return 1
    }

    private fun resetTime(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val id = skill.id
        val config = context.skillConfig
        config.getModifier(id)?.run {
            time = null
            if (isEmpty) {
                config.removeModifier(id)
            }
            context.syncConfig()
        }
        context.sendFeedback(
            "time.reset",
            skill.name
        )
        return 1
    }

    private fun resetRarity(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val id = skill.id
        val config = context.skillConfig
        config.getModifier(id)?.run {
            rarity = null
            if (isEmpty) {
                config.removeModifier(id)
            }
            context.syncConfig()
        }
        context.sendFeedback(
            "rarity.reset",
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
                "rarity.invalid",
                rarityStr
            )
            0
        } else {
            context.skillConfig.getOrCreateModifier(skill.id).rarity = rarity
            context.syncConfig()
            context.sendFeedback(
                "rarity.set",
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
        val id = skill.id
        val config = context.skillConfig
        config.getModifier(id)?.run {
            cooldown = null
            if (isEmpty) {
                config.removeModifier(id)
            }
            context.syncConfig()
        }
        context.sendFeedback(
            "cooldown.reset",
            skill.name
        )
        return 1
    }

    private fun setCooldown(context: CommandContext<ServerCommandSource>): Int {
        val skill = context.getSkill()
        val seconds = IntegerArgumentType.getInteger(context, "seconds")
        context.skillConfig.getOrCreateModifier(skill.id).cooldown = seconds * 20
        context.syncConfig()
        context.sendFeedback(
            "cooldown.set",
            skill.name,
            seconds
        )
        return 1
    }

    private fun CommandContext<ServerCommandSource>.getSkill() =
        SkillArgumentType.getSkill(this)
}