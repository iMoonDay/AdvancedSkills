package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.server.command.*

object CooldownCommand : BaseCommand("cooldown") {

    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
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
                    "cooldownMultiplier.value",
                    context.skillConfig.skillCooldownMultiplier,
                )
            )
            1
        }

    private fun setMultiplier(
        context: CommandContext<ServerCommandSource>,
        multiplier: Double,
    ): Int {
        val config = context.skillConfig
        config.skillCooldownMultiplier = multiplier
        context.syncConfig()
        context.sendFeedback(
            "cooldownMultiplier.set",
            config.skillCooldownMultiplier
        )
        return 1
    }
}