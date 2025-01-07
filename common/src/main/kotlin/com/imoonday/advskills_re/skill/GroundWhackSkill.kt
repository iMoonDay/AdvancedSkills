package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*
import kotlin.math.*

class GroundWhackSkill : Skill(
    Settings(
        id = "ground_whack",
        types = listOf(SkillType.ATTACK, SkillType.MOVEMENT),
        cooldown = 8,
        rarity = SkillRarity.RARE
    )
), LandingTrigger, PersistentTrigger, FallTrigger, DangerTrigger {

    init {
        settings
            .addParameter(PARAM_WHACK_SOUND, DEFAULT_WHACK_SOUND)
            .addParameter(
                name = PARAM_FALL_SPEED,
                baseValue = DEFAULT_FALL_SPEED,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_WHACK_RANGE,
                baseValue = DEFAULT_WHACK_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_WHACK_DAMAGE,
                baseValue = DEFAULT_WHACK_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_KNOCKBACK_BOOST,
                baseValue = DEFAULT_KNOCKBACK_BOOST,
                enhancementId = ENHANCEMENT_KNOCKBACK,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_FALL_REDUCTION,
                baseValue = DEFAULT_FALL_REDUCTION,
                enhancementId = ENHANCEMENT_REDUCTION,
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_IMMUNE_HEIGHT,
                baseValue = DEFAULT_IMMUNE_HEIGHT,
                enhancementId = ENHANCEMENT_IMMUNE_HEIGHT,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_EFFECT_HEIGHT,
                baseValue = DEFAULT_EFFECT_HEIGHT,
                enhancementId = ENHANCEMENT_EFFECT_HEIGHT,
                value = 2.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isOnGround) return UseResult.fail(failedMessage)
        user.run {
            if (abilities.flying) abilities.flying = false
            val velocityMultiplier = getDoubleParam(PARAM_FALL_SPEED, this, DEFAULT_FALL_SPEED)
            val velocityY = min(velocity.y, -1.0) * velocityMultiplier
            velocity = Vec3d(0.0, velocityY, 0.0)
            updateVelocity()
            sendPacket(PlayerAbilitiesS2CPacket(abilities))
            startUsing()
        }
        return UseResult.success()
    }

    override fun onLanding(player: ServerPlayerEntity, height: Float) {
        if (!player.isUsing()) return
        if (height > 0) {
            val maxHeight = getDoubleParam(PARAM_EFFECT_HEIGHT, player, DEFAULT_EFFECT_HEIGHT)
            val newHeight = min(height.toDouble(), maxHeight)
            val damageMultiplier = getFloatParam(PARAM_WHACK_DAMAGE, player, DEFAULT_WHACK_DAMAGE)
            val damage = damageMultiplier * min(newHeight / 2, 5.0).toFloat()
            val knockbackBonus = getDoubleParam(PARAM_KNOCKBACK_BOOST, player, DEFAULT_KNOCKBACK_BOOST)
            val velocity = min(newHeight / 5, 2.0) + knockbackBonus
            val rangeMultiplier = getDoubleParam(PARAM_WHACK_RANGE, player, DEFAULT_WHACK_RANGE)

            player.world.getOtherEntities(
                player,
                player.boundingBox.expand(newHeight * rangeMultiplier)
            ) { it.isLiving && it.isAlive && !it.isSpectator && (player.y - it.y).absoluteValue <= 1 }
                .forEach {
                    it.damage(player.damageSources.playerAttack(player), damage)
                    it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(velocity))
                    (it as? ServerPlayerEntity)?.updateVelocity()
                }
            player.spawnParticles(
                ParticleTypes.CLOUD,
                false,
                player.pos,
                (100 * newHeight).toInt(),
                newHeight,
                0.0,
                newHeight,
                0.1
            )
            player.playSoundFromParam(PARAM_WHACK_SOUND, DEFAULT_WHACK_SOUND)
        }
        player.stopUsing()
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        if (!player.isUsing()) return amount
        val immuneHeight = getDoubleParam(PARAM_IMMUNE_HEIGHT, player, DEFAULT_IMMUNE_HEIGHT)
        val reduction = getDoubleParam(PARAM_FALL_REDUCTION, player, DEFAULT_FALL_REDUCTION)
        return if (fallDistance < immuneHeight) 0 else (amount * reduction).toInt()
    }

    companion object {

        // Default Values
        private const val DEFAULT_FALL_SPEED = 1.0
        private const val DEFAULT_WHACK_RANGE = 1.0
        private const val DEFAULT_WHACK_DAMAGE = 1.0f
        private const val DEFAULT_KNOCKBACK_BOOST = 0.0
        private const val DEFAULT_FALL_REDUCTION = 0.5
        private const val DEFAULT_IMMUNE_HEIGHT = 10.0
        private const val DEFAULT_EFFECT_HEIGHT = 20.0
        private val DEFAULT_WHACK_SOUND = SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP

        // Parameter Names
        private const val PARAM_WHACK_SOUND = "whack_sound"  // 重击音效
        private const val PARAM_FALL_SPEED = "fall_speed"  // 下落速度
        private const val PARAM_WHACK_RANGE = "whack_range"  // 重击范围
        private const val PARAM_WHACK_DAMAGE = "whack_damage"  // 重击伤害
        private const val PARAM_KNOCKBACK_BOOST = "knockback_boost"  // 击退提升
        private const val PARAM_FALL_REDUCTION = "fall_reduction"  // 坠落减免
        private const val PARAM_IMMUNE_HEIGHT = "immune_height"  // 免疫高度
        private const val PARAM_EFFECT_HEIGHT = "effect_height"  // 效果高度

        // Enhancement IDs
        private const val ENHANCEMENT_SPEED = "speed"  // 对应下落速度
        private const val ENHANCEMENT_RANGE = "range"  // 对应重击范围
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应重击伤害
        private const val ENHANCEMENT_KNOCKBACK = "knockback"  // 对应击退提升
        private const val ENHANCEMENT_REDUCTION = "reduction"  // 对应坠落减免
        private const val ENHANCEMENT_IMMUNE_HEIGHT = "immune_height"  // 对应免疫高度
        private const val ENHANCEMENT_EFFECT_HEIGHT = "effect_height"  // 对应效果高度
    }
}