package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.imoonday.advskills_re.util.SkillContainer.Companion.MAX_SLOT_SIZE
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object EquipCommand : PlayerCommand("equip") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            argument("skill", SkillArgumentType.validSkill())
                .then(
                    argument("slot", IntegerArgumentType.integer(1, MAX_SLOT_SIZE))
                        .executesWithPlayer(::equip)
                )
        )

    private fun equip(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val skill = SkillArgumentType.getSkill(context)
        val slot = IntegerArgumentType.getInteger(context, "slot")
        return if (player.equip(skill, slot)) {
            context.sendFeedback(
                "equipSkill.success",
                player.displayName,
                skill.name,
                slot
            )
            1
        } else {
            context.sendFeedback(
                "equipSkill.failed",
                player.displayName,
                skill.name,
                slot
            )
            0
        }
    }
}