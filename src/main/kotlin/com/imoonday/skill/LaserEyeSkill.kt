package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.spawnParticles
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.particle.DustParticleEffect
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import org.joml.Vector3f

class LaserEyeSkill : Skill(
    id = "laser_eye",
    types = arrayOf(SkillType.ATTACK),
    cooldown = 15,
    rarity = Rarity.EPIC,
    sound = ModSounds.LASER
) {

    private val particleColor = Vector3f(237 / 255f, 47 / 255f, 50 / 255f)

    override fun use(user: ServerPlayerEntity): UseResult {
        val cameraPos = user.getCameraPosVec(0f)
        val maxDistance = user.raycast(10.0).let {
            if (it.type == HitResult.Type.MISS) 10.0 else it.pos.distanceTo(cameraPos)
        }
        var offset = 0.1
        while (offset <= maxDistance) {
            user.spawnParticles(
                DustParticleEffect(particleColor, 1f),
                user.eyePos.x + user.rotationVector.x * offset,
                user.eyePos.y + user.rotationVector.y * offset,
                user.eyePos.z + user.rotationVector.z * offset,
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

    private fun PlayerEntity.raycast(maxDistance: Double): HitResult {
        val vec3d: Vec3d = getCameraPosVec(0f)
        val vec3d2: Vec3d = getRotationVec(0f)
        val vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance)
        return world.raycast(
            RaycastContext(
                vec3d,
                vec3d3,
                RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,
                this
            )
        )
    }
}