package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
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
    id = "ground_whack",
    types = listOf(SkillType.ATTACK, SkillType.MOVEMENT),
    cooldown = 8,
    rarity = SkillRarity.RARE,
    enhancements = setOf(
        SkillEnhancements.VELOCITY,
        SkillEnhancements.RANGE,
        SkillEnhancements.DAMAGE,
        SkillEnhancements.POWER
    )
), LandingTrigger, PersistentTrigger, FallTrigger, DangerTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isOnGround) return UseResult.fail(failedMessage())
        user.run {
            if (abilities.flying) abilities.flying = false
            val velocityY = min(velocity.y, -1.0) * (1.0 + user.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1)
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
            val newHeight = min(height.toDouble(), 20.0)
            val damage = getEnhancedValue(player, SkillEnhancements.DAMAGE, min(newHeight / 2, 5.0).toFloat())
            val velocity = min(newHeight / 5, 2.0) + player.getEnhancementLvl(SkillEnhancements.POWER) * 0.1
            val rangeMultiplier = 1.0 + player.getEnhancementLvl(SkillEnhancements.RANGE) * 0.1
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
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
        }
        player.stopUsing()
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        if (!player.isUsing()) return amount
        return if (fallDistance < 10) 0 else amount / 2
    }
}