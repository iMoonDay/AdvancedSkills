package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.util.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.particle.DustParticleEffect
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import org.joml.Vector3f

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
        val maxDistance = user.raycastVisualBlock(10.0).let {
            if (it.type == HitResult.Type.MISS) 10.0 else it.pos.distanceTo(cameraPos)
        }
        var offset = 0.1
        while (offset <= maxDistance) {
            user.spawnParticles(
                DustParticleEffect(particleColor, 1f),
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
        entities.forEach { it.damage(user.damageSources.magic(), 4f) }
        return UseResult.success()
    }
}