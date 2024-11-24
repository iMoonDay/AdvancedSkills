package com.imoonday.command

import com.imoonday.skill.*
import com.imoonday.util.*
import com.imoonday.util.SkillContainer.Companion.MAX_SLOT_SIZE
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object UnequipCommand : PlayerCommand("unequip") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.then(
            argument("slot", IntegerArgumentType.integer(1, MAX_SLOT_SIZE))
                .executesWithPlayer(::unequip)
        )

    private fun unequip(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        val slot = IntegerArgumentType.getInteger(context, "slot")
        val original = player.getSkill(slot)
        return if (player.equip(Skill.EMPTY, slot)) {
            context.sendFeedback(
                "unequipSkill", "success",
                player.displayName.string,
                original.name.string,
                slot
            )
            1
        } else {
            context.sendFeedback(
                "unequipSkill", "failed",
                player.displayName.string,
                slot
            )
            0
        }
    }
}
