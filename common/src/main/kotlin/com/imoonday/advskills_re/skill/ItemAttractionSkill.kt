package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ItemAttractionSkill : LongPressSkill(
    Settings(
        id = "item_attraction",
        types = listOf(SkillType.UTILITY),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), UsingRenderTrigger, GlowingTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("attract_exp", false, "exp")
            .addParameter(
                name = timeParamName,
                baseValue = 10 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "range",
                baseValue = 15.0,
                enhancementId = "range",
                value = 3.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = "velocity",
                baseValue = 0.25,
                enhancementId = "velocity",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super.tick(player, usedTime)
        if (!player.isUsing()) return

        val range = getRange(player)
        val velocity = getDoubleParam("velocity", player, 0.25)
        val world = player.world
        world.getOtherEntities(
            player,
            player.boundingBox.expand(range)
        ) { checkAttractiveEntity(player, it) }.forEach {
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

    private fun checkAttractiveEntity(player: PlayerEntity, entity: Entity) =
        entity is ItemEntity && !entity.cannotPickup() ||
            getBooleanParam("attract_exp", player, false) && entity is ExperienceOrbEntity

    private fun getRange(player: PlayerEntity) =
        getDoubleParam("range", player, 15.0)

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        (clientPlayer.isUsing() && checkAttractiveEntity(clientPlayer, entity)
            && clientPlayer.boundingBox.expand(getRange(clientPlayer)).contains(entity.pos))
}