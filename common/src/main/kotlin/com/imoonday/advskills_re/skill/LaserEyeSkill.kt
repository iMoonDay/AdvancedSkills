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
            .addParameter("laser_sound", ModSounds.LASER)
            .addParameter(
                name = "launch_count",
                baseValue = 1,
                enhancementId = "count",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "distance",
                baseValue = 64.0,
                enhancementId = "distance",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage",
                baseValue = 8.0f,
                enhancementId = "damage",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val times = getIntParam("launch_count", user, 1)
        val distance = getDoubleParam("distance", user, 64.0)
        val damage = getFloatParam("damage", user, 8.0f)
        val sound = getSoundEventParam("laser_sound", ModSounds.LASER.get())

        user.executeAndAddTask(5, times) {
            execute(user, distance, damage, sound)
            true
        }
        return UseResult.success()
    }

    private fun execute(player: ServerPlayerEntity, distance: Double, damage: Float, sound: SoundEvent?) {
        val cameraPos = player.getCameraPosVec(0f)
        val maxDistance = player.raycastVisualBlock(distance).let {
            if (it.type == HitResult.Type.MISS) distance else it.pos.distanceTo(cameraPos)
        }
        val particles: MutableList<ParticleS2CPacket> = mutableListOf()
        var offset = 0.1
        while (offset <= maxDistance) {
            val pos = player.eyePos + player.rotationVector * offset
            particles += ParticleS2CPacket(
                DustParticleEffect(particleColor, 1f),
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

        private val particleColor = Vector3f(237 / 255f, 47 / 255f, 50 / 255f)
    }
}