package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*

class TeleportSkill : Skill(
    id = "teleport",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 2,
    rarity = SkillRarity.UNCOMMON,
    enhancements = setOf(SkillEnhancements.DISTANCE),
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            var distance = 2.0 + user.getEnhancementLvl(SkillEnhancements.DISTANCE) * 0.5
            val rotation = horizontalRotationVector.normalize()
            var offset = rotation.multiply(distance)
            var collisions = world.getBlockCollisions(this, boundingBox.offset(offset))
            while (!collisions.all { it.isEmpty }) {
                distance -= 0.1
                if (distance <= 0.0) {
                    return UseResult.fail(message("collide"))
                }
                offset = rotation.multiply(distance)
                collisions = world.getBlockCollisions(this, boundingBox.offset(offset))
                while (!collisions.all { it.isEmpty }) {
                    offset = offset.offset(Direction.UP, 0.05)
                    if (offset.y > 0.5) break
                    collisions = world.getBlockCollisions(this, boundingBox.offset(offset))
                }
            }
            val velocity = velocity
            val prevPos = centerPos
            requestTeleportOffset(offset.x, offset.y, offset.z)
            this.velocity = velocity
            updateVelocity()
            world.playSound(
                null,
                prevPos.x,
                prevPos.y,
                prevPos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            )
            user.spawnParticles(
                ParticleTypes.LARGE_SMOKE,
                false,
                prevPos,
                10,
                width / 2.0,
                height / 2.0,
                width / 2.0,
                0.1
            )
        }
        return UseResult.success()
    }
}