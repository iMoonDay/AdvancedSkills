package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*

object AddXpCommand : XpCommand("add") {

    override fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("targets", EntityArgumentType.players())
                .then(
                    argument("amount", IntegerArgumentType.integer())
                        .executes(::addPoints)
                        .then(literal("points").executes(::addPoints))
                        .then(literal("levels").executes(::addLevels))
                )
        )

    private fun addPoints(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillExp += amount
        }
        if (targets.size == 1) {
            context.sendFeedback(
                "skillExp.give.single",
                amount,
                targets.first().displayName.string
            )
        } else {
            context.sendFeedback(
                "skillExp.give.multiple",
                amount,
                targets.size
            )
        }
        return targets.size
    }

    private fun addLevels(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillLevel += amount
        }
        if (targets.size == 1) {
            context.sendFeedback(
                "skillLevel.give.single",
                amount,
                targets.first().displayName.string
            )
        } else {
            context.sendFeedback(
                "skillLevel.give.multiple",
                amount,
                targets.size
            )
        }
        return targets.size
    }
}