package com.imoonday.command

import com.imoonday.util.*
import com.imoonday.util.SkillContainer.Companion.MAX_SLOT_SIZE
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object EquipCommand : PlayerCommand("equip") {

    override fun buildWithPlayer(argument: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): RequiredArgumentBuilder<ServerCommandSource, EntitySelector> =
        argument.then(
            argument("skill", SkillArgumentType.skill())
                .then(
                    argument("slot", IntegerArgumentType.integer(1, MAX_SLOT_SIZE))
                        .executesWithPlayer(::equip)
                )
        )

    private fun equip(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        val slot = IntegerArgumentType.getInteger(context, "slot")
        return if (player.equip(skill, slot)) {
            context.sendFeedback(
                "equipSkill", "success",
                player.displayName.string,
                skill.name.string,
                slot
            )
            1
        } else {
            context.sendFeedback(
                "equipSkill", "failed",
                player.displayName.string,
                skill.name.string,
                slot
            )
            0
        }
    }
}