package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class PiercingSkill : Skill(
    Settings(
        id = "piercing",
        types = listOf(SkillType.MOVEMENT, SkillType.ATTACK),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), AutoStopTrigger, DangerTrigger, GravityTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_PIERCE_SOUND, DEFAULT_PIERCE_SOUND)
            .addParameter(
                name = PARAM_PIERCE_DURATION,
                baseValue = DEFAULT_PIERCE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_PIERCE_DAMAGE,
                baseValue = DEFAULT_PIERCE_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_PIERCE_SPEED,
                baseValue = DEFAULT_PIERCE_SPEED,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult =
        UseResult.startUsing(user, this, NbtCompound().apply {
            val speed = getDoubleParam(PARAM_PIERCE_SPEED, user, DEFAULT_PIERCE_SPEED)
            val velocity = user.horizontalRotationVector.normalize().multiply(speed, 0.0, speed)
            putDouble(NBT_VELOCITY_X, velocity.x)
            putDouble(NBT_VELOCITY_Z, velocity.z)
        }) {
            user.stopFallFlying()
            val speed = getDoubleParam(PARAM_PIERCE_SPEED, user, DEFAULT_PIERCE_SPEED)
            user.velocity = user.horizontalRotationVector.normalize().multiply(speed, 0.0, speed)
            user.updateVelocity()
        }

    override fun onStop(player: ServerPlayerEntity) {
        player.velocity = Vec3d.ZERO
        player.updateVelocity()
        super.onStop(player)
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_PIERCE_DURATION, player, DEFAULT_PIERCE_DURATION, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (player.horizontalCollision) {
            onStop(player)
            player.stopUsing()
            return
        }

        val data = player.getActiveData()
        if (data.contains(NBT_VELOCITY_X) && data.contains(NBT_VELOCITY_Z)) {
            player.velocity = Vec3d(
                data.getDouble(NBT_VELOCITY_X),
                0.0,
                data.getDouble(NBT_VELOCITY_Z)
            )
            player.updateVelocity()
        }

        val damage = getFloatParam(PARAM_PIERCE_DAMAGE, player, DEFAULT_PIERCE_DAMAGE)
        val speed = getDoubleParam(PARAM_PIERCE_SPEED, player, DEFAULT_PIERCE_SPEED)
        player.world.getOtherEntities(
            player, player.boundingBox
        ) { it is LivingEntity }.forEach {
            it.damage(player.damageSources.playerAttack(player), damage)
            it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(speed).withAxis(Direction.Axis.Y, 1.0))
            it.velocityDirty = true
            (it as? ServerPlayerEntity)?.updateVelocity()
        }
        super.serverTick(player, usedTime)
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (!player.isUsing()) return
        val pos = player.pos
        val halfHeight = player.height / 2.0
        val random = player.random
        for (i in 0 until 20) {
            player.world.addParticle(
                DustParticleEffect.DEFAULT,
                pos.x + random.nextDouble() - 0.5, pos.y + halfHeight, pos.z + random.nextDouble() - 0.5,
                0.0, 0.0, 0.0
            )
        }
    }

    companion object {

        // NBT Keys
        private const val NBT_VELOCITY_X = "x"  // X轴速度
        private const val NBT_VELOCITY_Z = "z"  // Z轴速度

        // Default Values
        private const val DEFAULT_PIERCE_DURATION = 8
        private const val DEFAULT_PIERCE_DAMAGE = 6.0f
        private const val DEFAULT_PIERCE_SPEED = 1.5
        private val DEFAULT_PIERCE_SOUND = ModSounds.PIERCING

        // Parameter Names
        private const val PARAM_PIERCE_SOUND = "pierce_sound"  // 穿刺音效
        private const val PARAM_PIERCE_DURATION = "pierce_duration"  // 穿刺持续时间
        private const val PARAM_PIERCE_DAMAGE = "pierce_damage"  // 穿刺伤害
        private const val PARAM_PIERCE_SPEED = "pierce_speed"  // 穿刺速度

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应伤害
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度
    }
}