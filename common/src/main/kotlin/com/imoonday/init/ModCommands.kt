package com.imoonday.init

import com.imoonday.skill.Skill
import com.imoonday.util.*
import com.imoonday.util.SkillContainer.Companion.MAX_SLOT_SIZE
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

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
                            .then(executeSlot())
                            .then(executeReset())
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

    private fun executeReset(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("reset").executes { context ->
            val target = getTarget(context)
            target.resetData()
            context.source.sendFeedback(
                { translate("reset", null, target.displayName.string) },
                true
            )
            1
        }

    private fun executeSlot(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("slot")
            .then(
                literal("add").then(
                    literal("active").executes { context ->
                        val target = getTarget(context)
                        val index =
                            (target.skillContainer.getLastSlot { it is SkillSlot.Active }?.index ?: 0) + 1
                        target.skillContainer.addSlot(SkillSlot.Active(index))
                            .also { target.syncData() } ?: 0
                    }
                ).then(
                    literal("generic").executes { context ->
                        val target = getTarget(context)
                        val index = (target.skillContainer.getLastSlot { it is SkillSlot.Generic }?.index
                            ?: target.skillContainer.getLastSlot { it is SkillSlot.Active }?.index
                            ?: 0) + 1
                        target.skillContainer.addSlot(SkillSlot.Generic(index))
                            .also { target.syncData() } ?: 0
                    }
                ).then(
                    literal("passive").executes { context ->
                        val target = getTarget(context)
                        val index = (target.skillContainer.getLastSlot { it is SkillSlot.Passive }?.index
                            ?: target.skillContainer.getLastSlot { it is SkillSlot.Generic }?.index
                            ?: target.skillContainer.getLastSlot { it is SkillSlot.Active }?.index
                            ?: 0) + 1
                        target.skillContainer.addSlot(SkillSlot.Passive(index))
                            .also { target.syncData() } ?: 0
                    }
                )
            ).then(
                literal("remove").then(
                    argument("index", IntegerArgumentType.integer(1, 10)).executes {
                        val target = getTarget(it)
                        val index = IntegerArgumentType.getInteger(it, "index")
                        target.skillContainer.removeSlot(index)?.let { 1 }.also { target.syncData() } ?: 0
                    }
                )
            ).then(
                literal("reset").executes {
                    val target = getTarget(it)
                    target.skillContainer.resetSlots().also { target.syncData() }
                    1
                }
            )

    private fun executeQuery(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("query")
            .then(
                argument("target", EntityArgumentType.player())
                    .executes(::executeQueryPoints)
                    .then(literal("points").executes(::executeQueryPoints))
                    .then(literal("levels").executes(::executeQueryLevels))
            )

    private fun executeQueryPoints(context: CommandContext<ServerCommandSource>): Int {
        val target = getTarget(context)
        val exp = target.skillExp

        context.source.sendFeedback(
            {
                translate(
                    "skillExp", "query",
                    target.displayName.string, exp
                )
            },
            true
        )
        return 1
    }

    private fun executeQueryLevels(context: CommandContext<ServerCommandSource>): Int {
        val target = getTarget(context)
        val level = target.skillLevel

        context.source.sendFeedback(
            {
                translate(
                    "skillLevel", "query",
                    target.displayName.string, level
                )
            },
            true
        )
        return 1
    }

    private fun executeSet(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("set")
            .then(
                argument("targets", EntityArgumentType.players()).then(
                    argument("amount", IntegerArgumentType.integer(0))
                        .executes(::executeSetPoints)
                        .then(literal("points").executes(::executeSetPoints))
                        .then(literal("levels").executes(::executeSetLevels))
                )
            )

    private fun executeSetPoints(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillExp = amount
        }
        if (targets.size == 1) {
            context.source.sendFeedback({
                translate(
                    "skillExp", "set.single",
                    amount,
                    targets.first().displayName.string
                )
            }, true)
        } else {
            context.source.sendFeedback({
                translate(
                    "skillExp", "set.multiple",
                    amount,
                    targets.size
                )
            }, true)
        }
        return targets.size
    }

    private fun executeSetLevels(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillLevel = amount
        }
        if (targets.size == 1) {
            context.source.sendFeedback({
                translate(
                    "skillLevel", "set.single",
                    amount,
                    targets.first().displayName.string
                )
            }, true)
        } else {
            context.source.sendFeedback({
                translate(
                    "skillLevel", "set.multiple",
                    amount,
                    targets.size
                )
            }, true)
        }
        return targets.size
    }

    private fun executeAdd(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("add")
            .then(
                argument("targets", EntityArgumentType.players())
                    .then(
                        argument("amount", IntegerArgumentType.integer())
                            .executes(::executeAddPoints)
                            .then(literal("points").executes(::executeAddPoints))
                            .then(literal("levels").executes(::executeAddLevels))
                    )
            )

    private fun executeAddPoints(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillExp += amount
        }
        if (targets.size == 1) {
            context.source.sendFeedback({
                translate(
                    "skillExp", "give.single",
                    amount,
                    targets.first().displayName.string
                )
            }, true)
        } else {
            context.source.sendFeedback({
                translate(
                    "skillExp", "give.multiple",
                    amount,
                    targets.size
                )
            }, true)
        }
        return targets.size
    }

    private fun executeAddLevels(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillLevel += amount
        }
        if (targets.size == 1) {
            context.source.sendFeedback({
                translate(
                    "skillLevel", "give.single",
                    amount,
                    targets.first().displayName.string
                )
            }, true)
        } else {
            context.source.sendFeedback({
                translate(
                    "skillLevel", "give.multiple",
                    amount,
                    targets.size
                )
            }, true)
        }
        return targets.size
    }

    private fun executeList(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("list").executes {
            val target = getTarget(it)
            val skills = target.learnedSkills

            if (skills.isNotEmpty())
                it.source.sendMessage(skills.joinToString(", ") { it.name.string }.toText())
            skills.size
        }

    private fun executeResetCooldown(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("reset-cooldown")
            .executes {
                val target = getTarget(it)
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
            IntegerArgumentType.integer(1, MAX_SLOT_SIZE)
        ).executes {
            val target = getTarget(it)
            val slot = IntegerArgumentType.getInteger(it, "slot")
            val original = target.getSkill(slot)
            if (target.equip(Skill.EMPTY, slot)) {
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
                IntegerArgumentType.integer(1, MAX_SLOT_SIZE)
            ).executes {
                val target = getTarget(it)
                val slot = IntegerArgumentType.getInteger(it, "slot")
                val skill = SkillArgumentType.getSkill(it)
                if (target.equip(skill, slot)) {
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
            target.forgetAll()
            1
        }

    private fun executeForget(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("forget")
            .then(argument("skill", SkillArgumentType.skill())
                .executes {
                    val target = getTarget(it)
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
            target.learnAll()
            1
        }

    private fun executeLearn(): LiteralArgumentBuilder<ServerCommandSource> =
        literal("learn")
            .then(argument("skill", SkillArgumentType.skill())
                .executes {
                    val target = getTarget(it)
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

    private fun getTarget(it: CommandContext<ServerCommandSource>): ServerPlayerEntity =
        EntityArgumentType.getPlayer(it, "target")
}