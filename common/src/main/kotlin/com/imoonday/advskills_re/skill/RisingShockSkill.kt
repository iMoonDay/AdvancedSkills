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
    Settings(
        id = "rising_shock",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 10,
        rarity = SkillRarity.RARE
    )
), AutoStopTrigger, GravityTrigger {

    init {
        settings
            .addParameter(PARAM_RISE_SOUND, DEFAULT_RISE_SOUND)
            .addParameter(
                name = PARAM_RISE_DURATION,
                baseValue = DEFAULT_RISE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_AFFECT_RANGE,
                baseValue = DEFAULT_AFFECT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.8,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_RISE_SPEED,
                baseValue = DEFAULT_RISE_SPEED,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.01,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this) {
        user.stopFallFlying()
        user.velocity = Vec3d(0.0, user.getRiseSpeed(), 0.0)
        user.updateVelocity()
    }.withSound(getSoundEventParam(PARAM_RISE_SOUND, DEFAULT_RISE_SOUND.get()))

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_RISE_DURATION, player, DEFAULT_RISE_DURATION, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        val riseSpeed = player.getRiseSpeed()
        player.velocity = Vec3d(0.0, riseSpeed, 0.0)
        player.updateVelocity()
        player.spawnParticles(
            ParticleTypes.CLOUD, false, Vec3d(player.x, player.boundingBox.minY, player.z), 10, 0.5, 0.5, 0.5, 0.1
        )
        val range = getDoubleParam(PARAM_AFFECT_RANGE, player, DEFAULT_AFFECT_RANGE)
        player.world.getOtherEntities(player, player.boundingBox.expand(range)) { it is LivingEntity }.forEach {
            it.velocity = it.velocity.withAxis(Direction.Axis.Y, max(it.velocity.y, riseSpeed))
            it.velocityDirty = true
            (it as? ServerPlayerEntity)?.updateVelocity()
        }
        super.serverTick(player, usedTime)
    }

    private fun PlayerEntity.getRiseSpeed(): Double {
        val multiplier = getDoubleParam(PARAM_RISE_SPEED, this, DEFAULT_RISE_SPEED)
        return max(velocity.y, 0.5) * multiplier
    }

    companion object {

        // Default Values
        private const val DEFAULT_RISE_DURATION = 8
        private const val DEFAULT_AFFECT_RANGE = 1.0
        private const val DEFAULT_RISE_SPEED = 1.0
        private val DEFAULT_RISE_SOUND = ModSounds.DASH

        // Parameter Names
        private const val PARAM_RISE_SOUND = "rise_sound"  // 上升音效
        private const val PARAM_RISE_DURATION = "rise_duration"  // 上升持续时间
        private const val PARAM_AFFECT_RANGE = "affect_range"  // 影响范围
        private const val PARAM_RISE_SPEED = "rise_speed"  // 上升速度

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度
    }
}