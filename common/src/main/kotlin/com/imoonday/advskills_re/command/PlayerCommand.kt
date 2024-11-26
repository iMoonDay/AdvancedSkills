package com.imoonday.advskills_re.command

import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

abstract class PlayerCommand(branch: String) : BaseCommand(branch) {

    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(buildWithTarget(argument("target", EntityArgumentType.player())))

    protected abstract fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *>

    protected fun getPlayer(context: CommandContext<ServerCommandSource>): ServerPlayerEntity =
        EntityArgumentType.getPlayer(context, "target")

    protected fun <S : ArgumentBuilder<ServerCommandSource, T>, T : ArgumentBuilder<ServerCommandSource, T>> S.executesWithPlayer(
        command: (CommandContext<ServerCommandSource>, ServerPlayerEntity) -> Int,
    ): T = executes { command(it, getPlayer(it)) }
}
