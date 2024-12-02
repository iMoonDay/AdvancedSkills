package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.screen.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.stat.*
import net.minecraft.text.*

class PortableChestSkill : Skill(
    id = "portable_chest",
    types = listOf(SkillType.FUNCTION),
    cooldown = 5,
    rarity = Rarity.SUPERB
), TickTrigger, UsingProgressTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.openHandledScreen(
            SimpleNamedScreenHandlerFactory(
                { syncId, inventory, _ ->
                    GenericContainerScreenHandler.createGeneric9x3(
                        syncId, inventory, user.enderChestInventory
                    )
                }, containerName
            )
        )
        user.incrementStat(Stats.OPEN_ENDERCHEST)
        user.playSound(SoundEvents.BLOCK_ENDER_CHEST_OPEN)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing() && (player.currentScreenHandler as ScreenHandlerAccessor).typeField != ScreenHandlerType.GENERIC_9X3) {
            player.playSound(SoundEvents.BLOCK_ENDER_CHEST_CLOSE)
            player.stopUsing()
            player.startCooling()
        }
    }

    override fun getProgress(player: PlayerEntity): Double = if (player.isUsing()) 1.0 else 0.0

    companion object {

        private val containerName = Text.translatable("container.enderchest")
    }
}