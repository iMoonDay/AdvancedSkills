package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ItemAttractionSkill : LongPressSkill(
    id = "item_attraction",
    types = listOf(SkillType.UTILITY),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.RANGE, SkillEnhancements.VELOCITY)
), UsingRenderTrigger, GlowingTrigger {

    override fun getMaxPressTime(): Int = 10 * 20

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super.tick(player, usedTime)
        if (!player.isUsing()) return

        val range = getRange(player)
        val velocity = 0.25 + player.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1
        val world = player.world
        world.getOtherEntities(
            player,
            player.boundingBox.expand(range)
        ) { it is ItemEntity && !it.cannotPickup() }.forEach {
            if (world.isClient) {
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
                it.velocity = player.eyePos.subtract(it.pos).normalize().multiply(velocity)
                it.velocityDirty = true
                if (it.horizontalCollision) it.addVelocity(0.0, 0.25, 0.0)
            }
        }
    }

    private fun getRange(player: PlayerEntity) =
        15.0 + player.getEnhancementLvl(SkillEnhancements.RANGE) * 3.0

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        (clientPlayer.isUsing() && entity is ItemEntity && !entity.cannotPickup()
            && clientPlayer.boundingBox.expand(getRange(clientPlayer)).contains(entity.pos))
}