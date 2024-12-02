package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.server.command.*
import net.minecraft.text.*

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
        ).then(
            literal("reset")
                .executes { resetSlots(it) }
        ).executes(::querySlots)
    }

    private fun setSlot(context: CommandContext<ServerCommandSource>, slotType: String): Int {
        val slot = IntegerArgumentType.getInteger(context, "slot")
        SkillConfig.get().setDefaultSkillSlot(slotType, slot)
        context.syncConfig()
        context.sendFeedback("defaultSlots.$slotType.set", slot)
        return 1
    }

    private fun querySlot(context: CommandContext<ServerCommandSource>, slotType: String): Int {
        val slot = SkillConfig.get().getDefaultSkillSlots(slotType)
        context.sendMessage(translate("defaultSlots.$slotType.query", slot))
        return 1
    }

    private fun querySlots(context: CommandContext<ServerCommandSource>): Int {
        val slots = SkillConfig.get().defaultSkillSlots
        var text = Text.empty()
        for ((slotType, slot) in slots) {
            text = text.append(translate("defaultSlots.$slotType.query", slot)).append(" ")
        }
        context.sendMessage(text)
        return 1
    }

    private fun resetSlots(context: CommandContext<ServerCommandSource>): Int {
        val slots = SkillConfig.get().defaultSkillSlots
        slots.clear()
        slots.putAll(SkillContainer.DEFAULT_SLOTS)
        context.syncConfig()
        context.sendFeedback("defaultSlots.reset")
        return 1
    }
}