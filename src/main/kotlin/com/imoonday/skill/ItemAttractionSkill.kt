package com.imoonday.skill

import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.ItemEntity
import net.minecraft.server.network.ServerPlayerEntity

class ItemAttractionSkill : LongPressSkill(
    id = "item_attraction",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB,
), UsingRenderTrigger {

    override fun getMaxPressTime(): Int = 20 * 10

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing())
            player.world.getOtherEntities(
                player,
                player.boundingBox.expand(15.0)
            ) { it is ItemEntity && !it.cannotPickup() }
                .forEach {
                    it.isGlowing = true
                    it.velocityDirty = true
                    it.velocity = player.eyePos.subtract(it.pos).normalize().multiply(0.25)
                    if (it.horizontalCollision) it.addVelocity(0.0, 0.25, 0.0)
                }
        super.tick(player, usedTime)
    }
}