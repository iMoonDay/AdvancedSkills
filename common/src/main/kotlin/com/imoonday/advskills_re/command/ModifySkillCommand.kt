package com.imoonday.advskills_re.command

//import com.imoonday.advskills_re.skill.enums.*
//import com.imoonday.advskills_re.util.*
//import com.mojang.brigadier.arguments.*
//import com.mojang.brigadier.builder.*
//import com.mojang.brigadier.context.*
//import com.mojang.brigadier.suggestion.*
//import net.minecraft.command.*
//import net.minecraft.server.command.*
//import java.util.concurrent.*

//object ModifySkillCommand : BaseCommand("modify") {
//
//    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
//        builder.then(
//            argument("skill", SkillArgumentType.validSkill())
//                .then(
//                    literal("cooldown")
//                        .then(
//                            literal("set")
//                                .then(
//                                    literal("local")
//                                        .then(
//                                            argument("seconds", IntegerArgumentType.integer(0))
//                                                .executes { setCooldown(it, false) }
//                                        )
//                                )
//                                .then(
//                                    literal("global")
//                                        .then(
//                                            argument("seconds", IntegerArgumentType.integer(0))
//                                                .executes { setCooldown(it, true) }
//                                        )
//                                )
//                        )
//                        .then(
//                            literal("reset")
//                                .then(literal("local").executes { resetCooldown(it, false) })
//                                .then(literal("global").executes { resetCooldown(it, true) })
//                        )
//                )
//                .then(
//                    literal("rarity")
//                        .then(
//                            literal("set")
//                                .then(
//                                    literal("local")
//                                        .then(
//                                            argument("rarity", StringArgumentType.word())
//                                                .suggests { _, builder1 -> suggestRarity(builder1) }
//                                                .executes { setRarity(it, false) }
//                                        )
//                                )
//                                .then(
//                                    literal("global")
//                                        .then(
//                                            argument("rarity", StringArgumentType.word())
//                                                .suggests { _, builder1 -> suggestRarity(builder1) }
//                                                .executes { setRarity(it, true) }
//                                        )
//                                )
//                        )
//                        .then(
//                            literal("reset")
//                                .then(literal("local").executes { resetRarity(it, false) })
//                                .then(literal("global").executes { resetRarity(it, true) })
//                        )
//                )
//                .then(
//                    literal("time")
//                        .then(
//                            literal("set")
//                                .then(
//                                    literal("local")
//                                        .then(
//                                            argument("seconds", IntegerArgumentType.integer(0))
//                                                .executes { setTime(it, false) }
//                                        )
//                                )
//                                .then(
//                                    literal("global")
//                                        .then(
//                                            argument("seconds", IntegerArgumentType.integer(0))
//                                                .executes { setTime(it, true) }
//                                        )
//                                )
//                        )
//                        .then(
//                            literal("reset")
//                                .then(literal("local").executes { resetTime(it, false) })
//                                .then(literal("global").executes { resetTime(it, true) })
//                        )
//                )
//        ).then(
//            literal("reset")
//                .then(literal("local").executes { resetAll(it, false) })
//                .then(literal("global").executes { resetAll(it, true) })
//        )
//
//    private fun setTime(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        val skill = context.getSkill()
//        val seconds = IntegerArgumentType.getInteger(context, "seconds")
//        getSkillConfig(global).getOrCreateModifier(skill.id).time = seconds * 20
//        trySave(global)
//        context.syncConfig(global)
//        context.sendFeedback(
//            "time.set",
//            skill.name,
//            seconds
//        )
//        return 1
//    }
//
//    private fun resetTime(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        val skill = context.getSkill()
//        val id = skill.id
//        val config = getSkillConfig(global)
//        config.getModifier(id)?.run {
//            time = null
//            if (isEmpty) {
//                config.removeModifier(id)
//            }
//            trySave(global)
//            context.syncConfig(global)
//        }
//        context.sendFeedback(
//            "time.reset",
//            skill.name
//        )
//        return 1
//    }
//
//    private fun resetRarity(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        val skill = context.getSkill()
//        val id = skill.id
//        val config = getSkillConfig(global)
//        config.getModifier(id)?.run {
//            rarity = null
//            if (isEmpty) {
//                config.removeModifier(id)
//            }
//            trySave(global)
//            context.syncConfig(global)
//        }
//        context.sendFeedback(
//            "rarity.reset",
//            skill.name
//        )
//        return 1
//    }
//
//    private fun setRarity(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        val skill = context.getSkill()
//        val rarityStr = StringArgumentType.getString(context, "rarity")
//        val rarity = SkillRarity.fromId(rarityStr)
//        return if (rarity == null) {
//            context.sendError(
//                "rarity.invalid",
//                rarityStr
//            )
//            0
//        } else {
//            getSkillConfig(global).getOrCreateModifier(skill.id).rarity = rarity
//            trySave(global)
//            context.syncConfig(global)
//            context.sendFeedback(
//                "rarity.set",
//                skill.name,
//                rarity.displayName
//            )
//            1
//        }
//    }
//
//    private fun suggestRarity(builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
//        CommandSource.suggestMatching(
//            SkillRarity.entries,
//            builder,
//            SkillRarity::id,
//            SkillRarity::displayName
//        )
//
//    private fun resetCooldown(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        val skill = context.getSkill()
//        val id = skill.id
//        val config = getSkillConfig(global)
//        config.getModifier(id)?.run {
//            cooldown = null
//            if (isEmpty) {
//                config.removeModifier(id)
//            }
//            trySave(global)
//            context.syncConfig(global)
//        }
//        context.sendFeedback(
//            "cooldown.reset",
//            skill.name
//        )
//        return 1
//    }
//
//    private fun setCooldown(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        val skill = context.getSkill()
//        val seconds = IntegerArgumentType.getInteger(context, "seconds")
//        getSkillConfig(global).getOrCreateModifier(skill.id).cooldown = seconds * 20
//        trySave(global)
//        context.syncConfig(global)
//        context.sendFeedback(
//            "cooldown.set",
//            skill.name,
//            seconds
//        )
//        return 1
//    }
//
//    private fun resetAll(context: CommandContext<ServerCommandSource>, global: Boolean): Int {
//        getSkillConfig(global).skillModifier.clear()
//        trySave(global)
//        context.syncConfig(global)
//        context.sendFeedback("resetModifiers")
//        return 1
//    }
//
//    private fun CommandContext<ServerCommandSource>.getSkill() =
//        SkillArgumentType.getSkill(this)
//}