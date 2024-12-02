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
                    SkillConfig.get().skillXpMultiplier,
                )
            )
            1
        }

    private fun setMultiplier(
        context: CommandContext<ServerCommandSource>,
        multiplier: Double,
    ): Int {
        SkillConfig.get().skillXpMultiplier = multiplier
        context.syncConfig()
        context.sendFeedback(
            "xpMultiplier.set",
            SkillConfig.get().skillXpMultiplier
        )
        return 1
    }
}