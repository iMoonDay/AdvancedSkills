package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object LearnAllCommand : PlayerCommand("learn-all") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.executesWithPlayer { _, player -> learnAll(player) }

    private fun learnAll(player: ServerPlayerEntity): Int {
        player.learnAll()
        return 1
    }
}