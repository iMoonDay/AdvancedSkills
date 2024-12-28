package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import kotlin.math.*

class RisingShockSkill : Skill(
    id = "rising_shock",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 10,
    rarity = SkillRarity.RARE,
    sound = ModSounds.DASH,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.RANGE, SkillEnhancements.VELOCITY)
), AutoStopTrigger, GravityTrigger {

    init {
        addEnhanceableParameter(
            timeParamName,
            8,
            "time",
            0.2f,
            Enhancement.Operation.MULTIPLY,
            5
        ) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.stopFallFlying()
        user.velocity = Vec3d(0.0, user.getVelocityY(), 0.0)
        user.updateVelocity()
        return UseResult.startUsing(user, this)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        val velocityY = player.getVelocityY()
        player.velocity = Vec3d(0.0, velocityY, 0.0)
        player.updateVelocity()
        player.spawnParticles(
            ParticleTypes.CLOUD,
            false,
            Vec3d(player.x, player.boundingBox.minY, player.z),
            10,
            0.5,
            0.5,
            0.5,
            0.1
        )
        val range = 1.0 + player.getEnhancementLvl(SkillEnhancements.RANGE) * 0.8
        player.world.getOtherEntities(player, player.boundingBox.expand(range)) { it is LivingEntity }
            .forEach {
                it.velocity = it.velocity.withAxis(Direction.Axis.Y, max(it.velocity.y, velocityY))
                it.velocityDirty = true
                (it as? ServerPlayerEntity)?.updateVelocity()
            }
        super.serverTick(player, usedTime)
    }

    private fun PlayerEntity.getVelocityY(): Double =
        max(velocity.y, 0.5) * (1.0 + this.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.01)
}