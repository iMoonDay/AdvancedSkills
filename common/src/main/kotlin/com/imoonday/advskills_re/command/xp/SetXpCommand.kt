package com.imoonday.advskills_re.command.xp

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*

object SetXpCommand : XpCommand("set") {

    override fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("targets", EntityArgumentType.players()).then(
                argument("amount", IntegerArgumentType.integer(0))
                    .executes(SetXpCommand::setPoints)
                    .then(literal("points").executes(SetXpCommand::setPoints))
                    .then(literal("levels").executes(SetXpCommand::setLevels))
            )
        )

    private fun setPoints(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillExp = amount
        }
        if (targets.size == 1) {
            context.sendFeedback(
                "skillExp.set.single",
                amount,
                targets.first().displayName
            )
        } else {
            context.sendFeedback(
                "skillExp.set.multiple",
                amount,
                targets.size
            )
        }
        return targets.size
    }

    private fun setLevels(context: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(context, "amount")
        val targets = EntityArgumentType.getPlayers(context, "targets")

        for (entity in targets) {
            entity.skillLevel = amount
        }
        if (targets.size == 1) {
            context.sendFeedback(
                "skillLevel.set.single",
                amount,
                targets.first().displayName
            )
        } else {
            context.sendFeedback(
                "skillLevel.set.multiple",
                amount,
                targets.size
            )
        }
        return targets.size
    }
}