package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*

object ResetXpCommand : XpCommand() {

    override fun build(): ArgumentBuilder<ServerCommandSource, *> =
        literal("reset")
            .then(
                argument("targets", EntityArgumentType.players())
                    .executes(::resetXp)
            )

    private fun resetXp(context: CommandContext<ServerCommandSource>): Int {
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillExp = 0
            entity.skillLevel = 0
        }
        if (targets.size == 1) {
            context.sendFeedback(
                "resetXp", "single",
                targets.first().displayName.string
            )
        } else {
            context.sendFeedback(
                "resetXp", "multiple",
                targets.size
            )
        }
        return targets.size
    }
}