package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object LearnAllCommand : PlayerCommand("learn-all") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.executesWithPlayer { _, player -> learnAll(player) }

    private fun learnAll(player: ServerPlayerEntity): Int {
        player.learnAll()
        return 1
    }
}