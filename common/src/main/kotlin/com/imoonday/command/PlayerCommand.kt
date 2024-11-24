package com.imoonday.command

import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.command.argument.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

abstract class PlayerCommand(branch: String) : BaseCommand(branch) {

    override fun build(): ArgumentBuilder<ServerCommandSource, *> =
        buildWithPlayer(argument("target", EntityArgumentType.player()))

    protected abstract fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector>

    protected fun getPlayer(context: CommandContext<ServerCommandSource>): ServerPlayerEntity =
        EntityArgumentType.getPlayer(context, "target")

    protected fun <S : ArgumentBuilder<ServerCommandSource, T>, T : ArgumentBuilder<ServerCommandSource, T>> S.executesWithPlayer(
        command: (CommandContext<ServerCommandSource>, ServerPlayerEntity) -> Int
    ): T = executes { command(it, getPlayer(it)) }
}
