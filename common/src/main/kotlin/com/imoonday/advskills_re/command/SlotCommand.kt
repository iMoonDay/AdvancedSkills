package com.imoonday.advskills_re.command

import com.imoonday.advskills_re.util.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.*
import net.minecraft.command.*
import net.minecraft.server.command.*
import net.minecraft.server.network.*

object SlotCommand : PlayerCommand("slot") {

    override fun buildWithTarget(builder: RequiredArgumentBuilder<ServerCommandSource, EntitySelector>): ArgumentBuilder<ServerCommandSource, *> =
        builder.then(
            literal("add")
                .then(
                    literal("active")
                        .executesWithPlayer { _, player -> addActive(player) }
                ).then(
                    literal("generic")
                        .executesWithPlayer { _, player -> addGeneric(player) })
                .then(
                    literal("passive")
                        .executesWithPlayer { _, player -> addPassive(player) }
                )
        ).then(
            literal("remove")
                .then(
                    argument("index", IntegerArgumentType.integer(1, 10))
                        .executesWithPlayer(::remove)
                )
        ).then(literal("reset").executesWithPlayer { _, player -> reset(player) })

    private fun reset(player: ServerPlayerEntity): Int {
        player.skillContainer.resetSlots()
        player.syncData()
        return 1
    }

    private fun remove(
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity,
    ): Int {
        val index = IntegerArgumentType.getInteger(context, "index")
        return player.skillContainer.removeSlot(index)?.run {
            player.syncData()
            1
        } ?: 0
    }

    private fun addPassive(player: ServerPlayerEntity): Int {
        val index = (player.skillContainer.getLastSlot(
            { it is SkillSlot.Passive },
            { it is SkillSlot.Generic },
            { it is SkillSlot.Active }
        )?.index ?: 0) + 1
        return player.skillContainer.addSlot(SkillSlot.Passive(index)).also { player.syncData() } ?: 0
    }

    private fun addGeneric(player: ServerPlayerEntity): Int {
        val index = (player.skillContainer.getLastSlot(
            { it is SkillSlot.Generic },
            { it is SkillSlot.Active }
        )?.index ?: 0) + 1
        return player.skillContainer.addSlot(SkillSlot.Generic(index)).also { player.syncData() } ?: 0
    }

    private fun addActive(player: ServerPlayerEntity): Int {
        val index = (player.skillContainer.getLastSlot { it is SkillSlot.Active }?.index ?: 0) + 1
        return player.skillContainer.addSlot(SkillSlot.Active(index)).also { player.syncData() } ?: 0
    }
}
