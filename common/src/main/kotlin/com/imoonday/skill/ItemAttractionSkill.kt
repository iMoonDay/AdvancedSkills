package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ItemAttractionSkill : LongPressSkill(
    id = "item_attraction",
    types = listOf(SkillType.FUNCTION),
    cooldown = 15,
    rarity = Rarity.SUPERB,
), UsingRenderTrigger, GlowingTrigger {

    override fun getMaxPressTime(): Int = 20 * 10

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        if (player.isUsing())
            player.world.getOtherEntities(
                player,
                player.boundingBox.expand(15.0)
            ) { it is ItemEntity && !it.cannotPickup() }
                .forEach {
                    if (player.world.isClient) {
                        it.world.addParticle(
                            ParticleTypes.ENCHANT,
                            it.x,
                            it.boundingBox.maxY,
                            it.z,
                            0.0,
                            0.25,
                            0.0
                        )
                    } else {
                        it.velocityDirty = true
                        it.velocity = player.eyePos.subtract(it.pos).normalize().multiply(0.25)
                        if (it.horizontalCollision) it.addVelocity(0.0, 0.25, 0.0)
                    }
                }
        super.tick(player, usedTime)
    }

    override fun isGlowing(entity: Entity): Boolean {
        val player = clientPlayer ?: return false
        return (player.isUsing() && entity is ItemEntity && !entity.cannotPickup()
            && player.boundingBox.expand(15.0).contains(entity.pos))
    }
}