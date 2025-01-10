package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*

class TeleportSkill : Skill(
    Settings(
        id = "teleport",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 2,
        rarity = SkillRarity.UNCOMMON
    )
) {

    init {
        settings
            .addParameter(PARAM_TELEPORT_SOUND, DEFAULT_TELEPORT_SOUND)
            .addParameter(
                name = PARAM_TELEPORT_RANGE,
                baseValue = DEFAULT_TELEPORT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.5,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            var distance = getDoubleParam(PARAM_TELEPORT_RANGE, user, DEFAULT_TELEPORT_RANGE)
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
            val sound = getSoundEventParam(PARAM_TELEPORT_SOUND, DEFAULT_TELEPORT_SOUND)
            sound?.let {
                world.playSound(
                    null, prevPos.x, prevPos.y, prevPos.z, it,
                    SoundCategory.PLAYERS, 1.0f, 1.0f
                )
            }
            spawnParticles(ParticleTypes.LARGE_SMOKE, false, prevPos, 10, width / 2.0, height / 2.0, width / 2.0, 0.1)
        }
        return UseResult.success()
    }

    companion object {

        // Default Values
        private const val DEFAULT_TELEPORT_RANGE = 2.0
        private val DEFAULT_TELEPORT_SOUND = SoundEvents.ENTITY_ENDERMAN_TELEPORT

        // Parameter Names
        private const val PARAM_TELEPORT_SOUND = "teleport_sound"  // 传送音效
        private const val PARAM_TELEPORT_RANGE = "teleport_range"  // 传送距离

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围
    }
}