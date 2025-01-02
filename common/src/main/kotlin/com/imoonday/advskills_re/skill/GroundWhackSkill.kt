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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("hit_sound", SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            .addParameter(
                name = "velocity_multiplier",
                baseValue = 1.0,
                enhancementId = "velocity",
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "range_multiplier",
                baseValue = 1.0,
                enhancementId = "range",
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage_multiplier",
                baseValue = 1.0f,
                enhancementId = "damage",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "knockback_bonus",
                baseValue = 0.0,
                enhancementId = "bonus",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "fall_damage_reduction",
                baseValue = 0.5,
                enhancementId = "reduction",
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "immune_fall_height",
                baseValue = 10.0,
                enhancementId = "immune_height",
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = "effect_max_height",
                baseValue = 20.0,
                enhancementId = "effect_height",
                value = 2.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isOnGround) return UseResult.fail(failedMessage())
        user.run {
            if (abilities.flying) abilities.flying = false
            val velocityMultiplier = getDoubleParam("velocity_multiplier", this, 1.0)
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
            val maxHeight = getDoubleParam("effect_max_height", player, 20.0)
            val newHeight = min(height.toDouble(), maxHeight)
            val damageMultiplier = getFloatParam("damage_multiplier", player, 1.0f)
            val damage = damageMultiplier * min(newHeight / 2, 5.0).toFloat()
            val knockbackBonus = getDoubleParam("knockback_bonus", player, 0.0)
            val velocity = min(newHeight / 5, 2.0) + knockbackBonus
            val rangeMultiplier = getDoubleParam("range_multiplier", player, 1.0)

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
            player.playSoundFromParam("hit_sound", SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
        }
        player.stopUsing()
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        if (!player.isUsing()) return amount
        val immuneHeight = getDoubleParam("immune_fall_height", player, 10.0)
        val reduction = getDoubleParam("fall_damage_reduction", player, 0.5)
        return if (fallDistance < immuneHeight) 0 else (amount * reduction).toInt()
    }
}