package com.imoonday.init

import com.imoonday.skill.Skill
import com.imoonday.util.*
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object ModCommands {

    fun init() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("skills").requires { it.hasPermissionLevel(2) }
                    .then(
                        argument("target", EntityArgumentType.player())
                            .then(executeLearn())
                            .then(executeLearnAll())
                            .then(executeForget())
                            .then(executeForgetAll())
                            .then(executeEquip())
                            .then(executeUnequip())
                            .then(executeResetCooldown())
                            .then(executeList())
                    )
            )

            dispatcher.register(
                literal("skill-xp").requires { it.hasPermissionLevel(2) }
                    .then(executeAdd())
                    .then(executeSet())
                    .then(executeQuery())
            )
        }
    }

    private fun executeQuery(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("query")
            .then(
                argument("target", EntityArgumentType.player()).executes {
                    val target = EntityArgumentType.getPlayer(it, "target")
                    val exp = target.skillExp

                    it.source.sendFeedback(
                        {
                            translate(
                                "skillExp", "query",
                                target.displayName.string, exp
                            )
                        },
                        true
                    )
                    1
                })

    private fun executeSet(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("set")
            .then(
                argument("targets", EntityArgumentType.players()).then(
                    argument("amount", LongArgumentType.longArg(0)).executes {
                        val amount = LongArgumentType.getLong(it, "amount")
                        val targets = EntityArgumentType.getPlayers(it, "targets")

                        for (entity in targets) {
                            entity.skillExp = amount
                        }
                        if (targets.size == 1) {
                            it.source.sendFeedback({
                                translate(
                                    "skillExp", "set.single",
                                    amount,
                                    targets.first().displayName.string
                                )
                            }, true)
                        } else {
                            it.source.sendFeedback({
                                translate(
                                    "skillExp", "set.multiple",
                                    amount,
                                    targets.size
                                )
                            }, true)
                        }
                        targets.size
                    })
            )

    private fun executeAdd(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("add")
            .then(
                argument("targets", EntityArgumentType.players())
                    .then(argument("amount", LongArgumentType.longArg()).executes {
                        val amount = LongArgumentType.getLong(it, "amount")
                        val targets = EntityArgumentType.getPlayers(it, "targets")

                        for (entity in targets) {
                            entity.skillExp += amount
                        }
                        if (targets.size == 1) {
                            it.source.sendFeedback({
                                translate(
                                    "skillExp", "give.single",
                                    amount,
                                    targets.first().displayName.string
                                )
                            }, true)
                        } else {
                            it.source.sendFeedback({
                                translate(
                                    "skillExp", "give.multiple",
                                    amount,
                                    targets.size
                                )
                            }, true)
                        }
                        targets.size
                    })
            )

    private fun executeList(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("list").executes {
            val target = EntityArgumentType.getPlayer(it, "target")
            val skills = target.learnedSkills

            it.source.sendMessage(
                Text.literal(skills.joinToString(", ") { it.name.string })
            )
            skills.size
        }

    private fun executeResetCooldown(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("reset-cooldown")
            .executes {
                val target = EntityArgumentType.getPlayer(it, "target")
                target.learnedSkills.forEach { target.stopCooling(it) }
                it.source.sendFeedback(
                    {
                        translate(
                            "resetCooldown",
                            null,
                            target.displayName.string
                        )
                    },
                    true
                )
                1
            }

    private fun executeUnequip(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("unequip").then(argument(
            "slot",
            IntegerArgumentType.integer(1, 4)
        ).executes {
            val target = EntityArgumentType.getPlayer(it, "target")
            val slot = IntegerArgumentType.getInteger(it, "slot")
            val skillSlot = SkillSlot.fromIndex(slot)
            val original = target.getSkill(skillSlot)
            if (target.equip(ModSkills.EMPTY, skillSlot)) {
                it.source.sendFeedback(
                    {
                        translate(
                            "unequipSkill",
                            "success",
                            target.displayName.string,
                            original.name.string,
                            slot
                        )
                    },
                    true
                )
            } else {
                it.source.sendFeedback(
                    {
                        translate(
                            "unequipSkill", "failed",
                            target.displayName.string,
                            slot
                        )
                    },
                    true
                )
            }
            1
        }
        )

    private fun executeEquip(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("equip").then(
            argument("skill", SkillArgumentType.skill()).then(argument(
                "slot",
                IntegerArgumentType.integer(1, 4)
            )
                .executes {
                    val target = EntityArgumentType.getPlayer(it, "target")
                    val slot = IntegerArgumentType.getInteger(it, "slot")
                    val skill = SkillArgumentType.getSkill(it)
                    if (target.equip(skill, SkillSlot.fromIndex(slot))) {
                        it.source.sendFeedback(
                            {
                                translate(
                                    "equipSkill",
                                    "success",
                                    target.displayName.string,
                                    skill.name.string,
                                    slot
                                )
                            },
                            true
                        )
                    } else {
                        it.source.sendFeedback(
                            {
                                translate(
                                    "equipSkill", "failed",
                                    target.displayName.string,
                                    skill.name.string,
                                    slot
                                )
                            },
                            true
                        )
                    }
                    1
                })
        )

    private fun executeForgetAll(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("forget-all").executes { context ->
            val target = EntityArgumentType.getPlayer(context, "target")
            target.run {
                learnedSkills.forEach {
                    forget(it)
                }
            }
            1
        }

    private fun executeForget(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("forget")
            .then(argument("skill", SkillArgumentType.skill())
                .executes {
                    val target = EntityArgumentType.getPlayer(it, "target")
                    val skill = SkillArgumentType.getSkill(it)
                    if (!target.forget(skill)) {
                        it.source.sendFeedback(
                            {
                                translate(
                                    "forgetSkill",
                                    "failed",
                                    target.displayName.string,
                                    skill.name.string
                                )
                            },
                            true
                        )
                    }
                    1
                })

    private fun executeLearnAll(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("learn-all").executes { context ->
            val target = EntityArgumentType.getPlayer(context, "target")
            Skill.getValidSkills().forEach { skill ->
                target.learn(skill)
            }
            1
        }

    private fun executeLearn(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("learn")
            .then(argument("skill", SkillArgumentType.skill())
                .executes {
                    val target = EntityArgumentType.getPlayer(it, "target")
                    val skill = SkillArgumentType.getSkill(it)
                    if (!target.learn(skill)) {
                        it.source.sendFeedback(
                            {
                                translate(
                                    "learnSkill", "failed",
                                    target.displayName.string,
                                    skill.name.string
                                )
                            },
                            true
                        )
                    }
                    1
                })
}