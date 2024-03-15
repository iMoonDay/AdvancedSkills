package com.imoonday.skill

import com.imoonday.trigger.GlowingTrigger
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.server.network.ServerPlayerEntity

class ItemAttractionSkill : LongPressSkill(
    id = "item_attraction",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB,
), UsingRenderTrigger, GlowingTrigger {

    override fun getMaxPressTime(): Int = 20 * 10

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing())
            player.world.getOtherEntities(
                player,
                player.boundingBox.expand(15.0)
            ) { it is ItemEntity && !it.cannotPickup() }
                .forEach {
                    it.velocityDirty = true
                    it.velocity = player.eyePos.subtract(it.pos).normalize().multiply(0.25)
                    if (it.horizontalCollision) it.addVelocity(0.0, 0.25, 0.0)
                }
        super.serverTick(player, usedTime)
    }

    override fun isGlowing(player: ClientPlayerEntity, entity: Entity): Boolean =
        player.isUsing() && entity is ItemEntity && !entity.cannotPickup()
            && player.boundingBox.expand(15.0).contains(entity.pos)
}