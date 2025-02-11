package com.imoonday.advskills_re.command.xp

import com.imoonday.advskills_re.command.*
import com.mojang.brigadier.builder.*
import net.minecraft.server.command.*

abstract class XpCommand(val action: String) : BaseCommand("xp") {

    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(buildAction(literal(action)))

    abstract fun buildAction(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *>
}