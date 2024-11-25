package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.util.*
import net.minecraft.entity.*
import net.minecraft.entity.projectile.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*
import org.joml.*

class LaserEyeSkill : Skill(
    id = "laser_eye",
    types = listOf(SkillType.ATTACK),
    cooldown = 15,
    rarity = Rarity.EPIC,
    sound = ModSounds.LASER
) {

    private val particleColor = Vector3f(237 / 255f, 47 / 255f, 50 / 255f)

    override fun use(user: ServerPlayerEntity): UseResult {
        val cameraPos = user.getCameraPosVec(0f)
        val maxDistance = user.raycastVisualBlock(64.0).let {
            if (it.type == HitResult.Type.MISS) 64.0 else it.pos.distanceTo(cameraPos)
        }
        var offset = 0.1
        while (offset <= maxDistance) {
            user.spawnParticles(
                DustParticleEffect(particleColor, 1f),
                true,
                user.eyePos + user.rotationVector * offset,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            )
            offset += 0.1
        }
        val entities: MutableList<LivingEntity> = mutableListOf()
        while (true) {
            ProjectileUtil.raycast(
                user,
                cameraPos,
                cameraPos.add(user.rotationVector.multiply(maxDistance)),
                Box.of(user.eyePos, 0.1, 0.1, 0.1).stretch(user.rotationVector.multiply(maxDistance)),
                { !it.isSpectator && it.isAlive && it.isLiving && it !in entities },
                maxDistance * maxDistance
            )?.takeUnless { it.type == HitResult.Type.MISS }?.let {
                entities.add(it.entity as LivingEntity)
            } ?: break
        }
        entities.forEach { it.damage(user.damageSources.magic(), 8f) }
        return UseResult.success()
    }
}