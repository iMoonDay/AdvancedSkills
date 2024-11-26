package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object QueryXpCommand : XpCommand("query") {

    override fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("target", EntityArgumentType.player())
                .executes(::queryPoints)
                .then(literal("points").executes(::queryPoints))
                .then(literal("levels").executes(::queryLevels))
        )

    private fun queryPoints(context: CommandContext<ServerCommandSource>): Int {
        val target = getPlayer(context)
        val exp = target.skillExp

        context.sendMessage(translate("skillExp.query", target.displayName.string, exp))
        return 1
    }

    private fun queryLevels(context: CommandContext<ServerCommandSource>): Int {
        val target = getPlayer(context)
        val level = target.skillLevel

        context.sendMessage(translate("skillLevel.query", target.displayName.string, level))
        return 1
    }

    private fun getPlayer(context: CommandContext<ServerCommandSource>): ServerPlayerEntity =
        EntityArgumentType.getPlayer(context, "target")
}