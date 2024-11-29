package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.server.command.*

object XpMultiplierCommand : XpCommand("multiplier") {

    override fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                .executes { context ->
                    val multiplier = DoubleArgumentType.getDouble(context, "multiplier")
                    setMultiplier(context, multiplier)
                }
        ).then(
            literal("reset")
                .executes { context ->
                    setMultiplier(context, 1.0)
                }
        ).executes { context ->
            context.sendMessage(
                translate(
                    "xpMultiplier.value",
                    context.skillConfig.skillXpMultiplier,
                )
            )
            1
        }

    private fun setMultiplier(
        context: CommandContext<ServerCommandSource>,
        multiplier: Double,
    ): Int {
        context.skillConfig.skillXpMultiplier = multiplier
        context.syncConfig()
        context.sendFeedback(
            "xpMultiplier.set",
            context.skillConfig.skillXpMultiplier
        )
        return 1
    }
}