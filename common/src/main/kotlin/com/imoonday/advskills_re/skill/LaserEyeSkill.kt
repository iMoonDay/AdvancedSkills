package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.projectile.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import org.joml.*

class LaserEyeSkill : Skill(
    Settings(
        id = "laser_eye",
        types = listOf(SkillType.ATTACK),
        cooldown = 15,
        rarity = SkillRarity.EPIC
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_LASER_SOUND, DEFAULT_LASER_SOUND)
            .addParameter(
                name = PARAM_LASER_COUNT,
                baseValue = DEFAULT_LASER_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_LASER_RANGE,
                baseValue = DEFAULT_LASER_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_LASER_DAMAGE,
                baseValue = DEFAULT_LASER_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val times = getIntParam(PARAM_LASER_COUNT, user, DEFAULT_LASER_COUNT)
        val range = getDoubleParam(PARAM_LASER_RANGE, user, DEFAULT_LASER_RANGE)
        val damage = getFloatParam(PARAM_LASER_DAMAGE, user, DEFAULT_LASER_DAMAGE)
        val sound = getSoundEventParam(PARAM_LASER_SOUND, DEFAULT_LASER_SOUND.get())

        user.executeAndAddTask(5, times) {
            execute(user, range, damage, sound)
            true
        }
        return UseResult.success()
    }

    private fun execute(player: ServerPlayerEntity, range: Double, damage: Float, sound: SoundEvent?) {
        val cameraPos = player.getCameraPosVec(0f)
        val maxDistance = player.raycastVisualBlock(range).let {
            if (it.type == HitResult.Type.MISS) range else it.pos.distanceTo(cameraPos)
        }
        val particles: MutableList<ParticleS2CPacket> = mutableListOf()
        var offset = 0.1
        while (offset <= maxDistance) {
            val pos = player.eyePos + player.rotationVector * offset
            particles += ParticleS2CPacket(
                DustParticleEffect(LASER_COLOR, 1f),
                true,
                pos.x, pos.y, pos.z,
                0f, 0f, 0f,
                0f, 1
            )
            offset += 0.1
        }
        player.serverWorld.players.forEach { it.sendPacket(BundleS2CPacket(particles)) }

        sound?.let { player.playSound(it) }

        val entities: MutableList<LivingEntity> = mutableListOf()
        while (true) {
            ProjectileUtil.raycast(
                player,
                cameraPos,
                cameraPos.add(player.rotationVector.multiply(maxDistance)),
                player.boundingBox.stretch(player.rotationVector.multiply(maxDistance)),
                { !it.isSpectator && it.isAlive && it.isLiving && it !in entities },
                maxDistance * maxDistance
            )?.takeUnless { it.type == HitResult.Type.MISS }?.let {
                entities.add(it.entity as LivingEntity)
            } ?: break
        }
        entities.forEach { it.damage(player.damageSources.magic(), damage) }
    }

    companion object {

        // Default Values
        private const val DEFAULT_LASER_COUNT = 1
        private const val DEFAULT_LASER_RANGE = 64.0
        private const val DEFAULT_LASER_DAMAGE = 8.0f
        private val DEFAULT_LASER_SOUND = ModSounds.LASER
        private val LASER_COLOR = Vector3f(237 / 255f, 47 / 255f, 50 / 255f)

        // Parameter Names
        private const val PARAM_LASER_SOUND = "laser_sound"  // 激光音效
        private const val PARAM_LASER_COUNT = "laser_count"  // 激光数量
        private const val PARAM_LASER_RANGE = "laser_range"  // 激光范围
        private const val PARAM_LASER_DAMAGE = "laser_damage"  // 激光伤害

        // Enhancement IDs
        private const val ENHANCEMENT_COUNT = "count"  // 对应激光数量
        private const val ENHANCEMENT_RANGE = "range"  // 对应激光范围
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应激光伤害
    }
}