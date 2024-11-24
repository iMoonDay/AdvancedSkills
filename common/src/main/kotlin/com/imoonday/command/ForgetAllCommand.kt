package com.imoonday.command

import com.imoonday.util.*
import com.mojang.brigadier.builder.*
import net.minecraft.command.*
import net.minecraft.server.command.*

object ForgetAllCommand : PlayerCommand("forget-all") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.executesWithPlayer { _, player ->
            player.forgetAll()
            1
        }
}