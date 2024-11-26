package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.builder.*
import net.minecraft.command.*
import net.minecraft.server.command.*

object ForgetAllCommand : PlayerCommand("forget-all") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.executesWithPlayer { _, player ->
            player.forgetAll()
            1
        }
}