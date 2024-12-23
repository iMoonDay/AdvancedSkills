package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.server.command.*

object XpMultiplierCommand : XpCommand("multiplier") {

    override fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            literal("set")
                .then(
                    literal("local")
                        .then(
                            argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                                .executes { context ->
                                    val multiplier = DoubleArgumentType.getDouble(context, "multiplier")
                                    setMultiplier(context, multiplier, false)
                                }
                        )
                )
                .then(
                    literal("global")
                        .then(
                            argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                                .executes { context ->
                                    val multiplier = DoubleArgumentType.getDouble(context, "multiplier")
                                    setMultiplier(context, multiplier, true)
                                }
                        )
                )
        ).then(
            literal("reset")
                .then(
                    literal("local")
                        .executes { context ->
                            setMultiplier(context, null, false)
                        }
                )
                .then(
                    literal("global")
                        .executes { context ->
                            setMultiplier(context, 1.0, true)
                        }
                )
        ).then(
            literal("get")
                .then(literal("local").executes(::getLocalMultiplier))
                .then(literal("global").executes(::getGlobalMultiplier))
                .executes(::getMultiplier)
        )

    private fun getMultiplier(context: CommandContext<ServerCommandSource>): Int {
        val value = getSkillConfig(false).skillXpMultiplier ?: getSkillConfig(true).skillXpMultiplier ?: 1.0
        context.sendMessage(translate("xpMultiplier.value", value))
        return 1
    }

    private fun getGlobalMultiplier(context: CommandContext<ServerCommandSource>): Int {
        context.sendMessage(
            getSkillConfig(true).skillXpMultiplier?.let {
                translate("xpMultiplier.value", it)
            } ?: translate("xpMultiplier.none")
        )
        return 1
    }

    private fun getLocalMultiplier(context: CommandContext<ServerCommandSource>): Int {
        context.sendMessage(
            getSkillConfig(false).skillXpMultiplier?.let {
                translate("xpMultiplier.value", it)
            } ?: translate("xpMultiplier.none")
        )
        return 1
    }

    private fun setMultiplier(
        context: CommandContext<ServerCommandSource>,
        multiplier: Double?,
        global: Boolean
    ): Int {
        getSkillConfig(global).skillXpMultiplier = multiplier
        trySave(global)
        context.syncConfig(global)
        if (multiplier != null) {
            context.sendFeedback("xpMultiplier.set", multiplier)
        } else {
            context.sendFeedback("xpMultiplier.reset")
        }
        return 1
    }
}