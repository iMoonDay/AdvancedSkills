package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.component.SkillContainer.Companion.MAX_SLOT_SIZE
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object UnequipCommand : PlayerCommand("unequip") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("slot", IntegerArgumentType.integer(1, MAX_SLOT_SIZE))
                .executesWithPlayer(::unequip)
        )

    private fun unequip(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val slot = IntegerArgumentType.getInteger(context, "slot")
        val original = player.getSkill(slot)
        return if (player.equip(Skills.EMPTY, slot)) {
            context.sendFeedback(
                "unequipSkill.success",
                player.displayName,
                original.name,
                slot
            )
            1
        } else {
            context.sendFeedback(
                "unequipSkill.failed",
                player.displayName,
                slot
            )
            0
        }
    }
}
