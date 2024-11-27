package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.nbt.*
import net.minecraft.server.command.*
import net.minecraft.text.*

abstract class BaseCommand(
    override val branch: String,
) : Command {

    override val root: String = "skills"
    protected lateinit var registry: CommandRegistryAccess
    protected lateinit var selection: CommandManager.RegistrationEnvironment

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registry: CommandRegistryAccess,
        selection: CommandManager.RegistrationEnvironment,
    ) {
        this.registry = registry
        this.selection = selection
        super.register(dispatcher, registry, selection)
    }

    protected fun literal(name: String): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal(name)

    protected fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<ServerCommandSource, T> =
        CommandManager.argument(name, type)

    protected fun CommandContext<ServerCommandSource>.sendFeedback(
        key: String,
        vararg args: Any,
    ) = source.sendFeedback({ translate(key, *args) }, true)

    protected fun CommandContext<ServerCommandSource>.sendError(
        key: String,
        vararg args: Any,
    ) = source.sendError(translate(key, *args))

    protected fun CommandContext<ServerCommandSource>.sendMessage(message: Text) = source.sendMessage(message)

    protected fun CommandContext<ServerCommandSource>.syncConfig() {
        Channels.SYNC_CONFIG_S2C.sendToPlayers(
            source.server.playerManager.playerList,
            SyncConfigS2CPacket(SkillConfig.instance.toTag(NbtCompound()))
        )
    }
}