package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.server.command.*

object DefaultSlotsCommand : BaseCommand("slots") {

    override fun build(builder: LiteralArgumentBuilder<ServerCommandSource>): ArgumentBuilder<ServerCommandSource, *> {
        return builder.then(
            literal("active")
                .then(
                    argument("slot", IntegerArgumentType.integer(0, 10))
                        .executes { setSlot(it, "active") }
                )
                .executes { querySlot(it, "active") }
        ).then(
            literal("generic")
                .then(
                    argument("slot", IntegerArgumentType.integer(0, 10))
                        .executes { setSlot(it, "generic") }
                )
                .executes { querySlot(it, "generic") }
        ).then(
            literal("passive")
                .then(
                    argument("slot", IntegerArgumentType.integer(0, 10))
                        .executes { setSlot(it, "passive") }
                )
                .executes { querySlot(it, "passive") }
        )
    }

    private fun setSlot(context: CommandContext<ServerCommandSource>, slotType: String): Int {
        val slot = IntegerArgumentType.getInteger(context, "slot")
        context.skillConfig.setDefaultSkillSlot(slotType, slot)
        context.syncConfig()
        context.sendFeedback("defaultSlots.$slotType.set", slot)
        return 1
    }

    private fun querySlot(context: CommandContext<ServerCommandSource>, slotType: String): Int {
        val slot = context.skillConfig.getDefaultSkillSlots(slotType)
        context.sendMessage(translate("defaultSlots.$slotType.query", slot))
        return 1
    }
}