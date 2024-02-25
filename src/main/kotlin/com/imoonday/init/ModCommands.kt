package com.imoonday.init

import com.imoonday.components.*
import com.imoonday.skills.Skills
import com.imoonday.utils.SkillArgumentType
import com.imoonday.utils.SkillSlot
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.Text

object ModCommands {
    fun init() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("skills")
                .requires { it.hasPermissionLevel(4) }
                .then(
                    argument("target", EntityArgumentType.player())
                        .then(
                            literal("learn")
                                .then(argument("skill", SkillArgumentType.skill())
                                    .executes {
                                        val target = EntityArgumentType.getPlayer(it, "target")
                                        val skill = SkillArgumentType.getSkill(it)
                                        if (!target.learnSkill(skill)) {
                                            it.source.sendFeedback(
                                                {
                                                    Text.translatable(
                                                        "advancedSkills.learnSkill.failed",
                                                        target.displayName.string,
                                                        skill.name.string
                                                    )
                                                },
                                                true
                                            )
                                        }
                                        1
                                    })

                        )
                        .then(literal("learn-all").executes { context ->
                            val target = EntityArgumentType.getPlayer(context, "target")
                            Skills.SKILLS.filterNot { it.isEmpty }.forEach { skill ->
                                target.learnSkill(skill)
                            }
                            1
                        })
                        .then(
                            literal("forget")
                                .then(argument("skill", SkillArgumentType.skill())
                                    .executes {
                                        val target = EntityArgumentType.getPlayer(it, "target")
                                        val skill = SkillArgumentType.getSkill(it)
                                        if (!target.forgetSkill(skill)) {
                                            it.source.sendFeedback(
                                                {
                                                    Text.translatable(
                                                        "advancedSkills.forgetSkill.failed",
                                                        target.displayName.string,
                                                        skill.name.string
                                                    )
                                                },
                                                true
                                            )
                                        }
                                        1
                                    })

                        )
                        .then(literal("forget-all").executes {
                            val target = EntityArgumentType.getPlayer(it, "target")
                            target.run {
                                learnedSkills.toList().forEach { skill ->
                                    forgetSkill(skill)
                                }
                            }
                            1
                        })
                        .then(
                            literal("equip").then(
                                argument("skill", SkillArgumentType.skill()).then(argument(
                                    "slot",
                                    IntegerArgumentType.integer(1, 4)
                                )
                                    .executes {
                                        val target = EntityArgumentType.getPlayer(it, "target")
                                        val slot = IntegerArgumentType.getInteger(it, "slot")
                                        val skill = SkillArgumentType.getSkill(it)
                                        if (target.equipSkill(SkillSlot.fromIndex(slot), skill)) {
                                            it.source.sendFeedback(
                                                {
                                                    Text.translatable(
                                                        "advancedSkills.equipSkill.success",
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
                                                    Text.translatable(
                                                        "advancedSkills.equipSkill.failed",
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
                        ).then(
                            literal("reset-cooldown")
                                .executes {
                                    val target = EntityArgumentType.getPlayer(it, "target")
                                    target.learnedSkills.forEach { target.stopCooling(it) }
                                    it.source.sendFeedback(
                                        {
                                            Text.translatable(
                                                "advancedSkills.resetCooldown",
                                                target.displayName.string
                                            )
                                        },
                                        true
                                    )
                                    1
                                }
                        )
                )
            )

            dispatcher.register(literal("skill-xp")
                .requires { it.hasPermissionLevel(2) }
                .then(
                    literal("add")
                        .then(
                            argument("targets", EntityArgumentType.players())
                                .then(argument("amount", IntegerArgumentType.integer()).executes {
                                    val amount = IntegerArgumentType.getInteger(it, "amount")
                                    val targets = EntityArgumentType.getPlayers(it, "targets")

                                    for (entity in targets) {
                                        entity.skillExp += amount
                                    }
                                    if (targets.size == 1) {
                                        it.source.sendFeedback({
                                            Text.translatable(
                                                "advancedSkills.skillExp.give.single",
                                                amount,
                                                targets.first().displayName.string
                                            )
                                        }, true)
                                    } else {
                                        it.source.sendFeedback({
                                            Text.translatable(
                                                "advancedSkills.skillExp.give.multiple",
                                                amount,
                                                targets.size
                                            )
                                        }, true)
                                    }
                                    targets.size
                                })
                        )
                )
                .then(
                    literal("set")
                        .then(
                            argument("targets", EntityArgumentType.players()).then(
                                argument("amount", IntegerArgumentType.integer()).executes {
                                    val amount = IntegerArgumentType.getInteger(it, "amount")
                                    val targets = EntityArgumentType.getPlayers(it, "targets")

                                    for (entity in targets) {
                                        entity.skillExp = amount
                                    }
                                    if (targets.size == 1) {
                                        it.source.sendFeedback({
                                            Text.translatable(
                                                "advancedSkills.skillExp.set.single",
                                                amount,
                                                targets.first().displayName.string
                                            )
                                        }, true)
                                    } else {
                                        it.source.sendFeedback({
                                            Text.translatable(
                                                "advancedSkills.skillExp.set.multiple",
                                                amount,
                                                targets.size
                                            )
                                        }, true)
                                    }
                                    targets.size
                                })
                        )
                ).then(
                    literal("query")
                        .then(
                            argument("target", EntityArgumentType.player()).executes {
                                val target = EntityArgumentType.getPlayer(it, "target")
                                val exp = target.skillExp

                                it.source.sendFeedback(
                                    {
                                        Text.translatable(
                                            "advancedSkills.skillExp.query",
                                            target.displayName.string, exp
                                        )
                                    },
                                    true
                                )
                                1
                            })
                )
            )
        }
    }
}