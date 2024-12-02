package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*

object ResetXpCommand : XpCommand("reset") {

    override fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("targets", EntityArgumentType.players())
                .executes(::resetXp)
        )

    private fun resetXp(context: CommandContext<ServerCommandSource>): Int {
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (player in targets) {
            player.levelData.reset()
            player.syncData()
        }
        if (targets.size == 1) {
            context.sendFeedback(
                "resetXp.single",
                targets.first().displayName
            )
        } else {
            context.sendFeedback(
                "resetXp.multiple",
                targets.size
            )
        }
        return targets.size
    }
}