package com.imoonday.command

import com.imoonday.config.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import net.minecraft.server.command.*

object CooldownCommand : BaseCommand("cooldown") {

    override fun build(): ArgumentBuilder<ServerCommandSource, *> =
        argument("multiplier", DoubleArgumentType.doubleArg(0.0))
            .executes { context ->
                val multiplier = DoubleArgumentType.getDouble(context, "multiplier")
                SkillConfig.instance.skillCooldownMultiplier = multiplier
                context.syncConfig()
                1
            }
}