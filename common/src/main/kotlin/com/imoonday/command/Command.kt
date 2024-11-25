package com.imoonday.command

import com.mojang.brigadier.*
import com.mojang.brigadier.builder.*
import dev.architectury.event.events.common.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.command.CommandManager.*

interface Command : CommandRegistrationEvent {

    val root: String
    val branch: String

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registry: CommandRegistryAccess,
        selection: RegistrationEnvironment,
    ) {
        dispatcher.register(literal(root).requires { it.hasPermissionLevel(2) }.then(buildBranch()))
    }

    fun buildBranch(): LiteralArgumentBuilder<ServerCommandSource> =
        literal(branch).then(build())

    fun build(): ArgumentBuilder<ServerCommandSource, *>

    companion object {

        fun CommandDispatcher<ServerCommandSource>.register(
            registry: CommandRegistryAccess,
            selection: RegistrationEnvironment, vararg commands: Command,
        ) = commands.forEach { it.register(this, registry, selection) }
    }
}