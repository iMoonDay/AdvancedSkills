package com.imoonday.init

import com.imoonday.component.*
import com.imoonday.util.SkillArgumentType
import com.imoonday.util.SkillSlot
import com.imoonday.util.translate
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal

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

                        )
                        .then(literal("learn-all").executes { context ->
                            val target = EntityArgumentType.getPlayer(context, "target")
                            ModSkills.SKILLS.filterNot { it.invalid }.forEach { skill ->
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
                                        if (target.equipSkill(skill, SkillSlot.fromIndex(slot))) {
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
                        ).then(
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
                        )
                )
            )

            dispatcher.register(literal("skill-xp")
                .requires { it.hasPermissionLevel(2) }
                .then(
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
                )
                .then(
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
                ).then(
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
                )
            )
        }
    }
}