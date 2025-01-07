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

    init {
        settings
            .addParameter(PARAM_ATTRACT_EXP, DEFAULT_ATTRACT_EXP, ENHANCEMENT_EXP)
            .addParameter(
                name = PARAM_ATTRACT_DURATION,
                baseValue = DEFAULT_ATTRACT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_ATTRACT_RANGE,
                baseValue = DEFAULT_ATTRACT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 3.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_ATTRACT_FORCE,
                baseValue = DEFAULT_ATTRACT_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
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
        val force = getDoubleParam(PARAM_ATTRACT_FORCE, player, DEFAULT_ATTRACT_FORCE)
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
                it.velocity = player.eyePos.subtract(it.pos).normalize().multiply(force)
                it.velocityDirty = true
                if (it.horizontalCollision) it.addVelocity(0.0, 0.25, 0.0)
            }
        }
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_ATTRACT_DURATION, player, DEFAULT_ATTRACT_DURATION, 0)

    private fun checkAttractiveEntity(player: PlayerEntity, entity: Entity) =
        entity is ItemEntity && !entity.cannotPickup() ||
            getBooleanParam(PARAM_ATTRACT_EXP, player, DEFAULT_ATTRACT_EXP) && entity is ExperienceOrbEntity

    private fun getRange(player: PlayerEntity) =
        getDoubleParam(PARAM_ATTRACT_RANGE, player, DEFAULT_ATTRACT_RANGE)

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        (clientPlayer.isUsing() && checkAttractiveEntity(clientPlayer, entity)
            && clientPlayer.boundingBox.expand(getRange(clientPlayer)).contains(entity.pos))

    companion object {

        // Default Values
        private const val DEFAULT_ATTRACT_DURATION = 10 * 20
        private const val DEFAULT_ATTRACT_RANGE = 15.0
        private const val DEFAULT_ATTRACT_FORCE = 0.25
        private const val DEFAULT_ATTRACT_EXP = false

        // Parameter Names
        private const val PARAM_ATTRACT_EXP = "attract_exp"  // 吸引经验球
        private const val PARAM_ATTRACT_DURATION = "attract_duration"  // 吸引持续时间
        private const val PARAM_ATTRACT_RANGE = "attract_range"  // 吸引范围
        private const val PARAM_ATTRACT_FORCE = "attract_force"  // 吸引力度

        // Enhancement IDs
        private const val ENHANCEMENT_EXP = "exp"  // 对应经验球吸引
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应吸引范围
        private const val ENHANCEMENT_FORCE = "force"  // 对应吸引力度
    }
}