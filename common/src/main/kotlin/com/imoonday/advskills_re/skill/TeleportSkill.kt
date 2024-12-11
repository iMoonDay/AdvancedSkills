package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*

class TeleportSkill : Skill(
    id = "teleport",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 2,
    rarity = SkillRarity.UNCOMMON
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val offset = rotationVector.withAxis(Direction.Axis.Y, 0.0).normalize().multiply(2.0)
            val collisions = world.getBlockCollisions(this, boundingBox.offset(offset))
            if (!collisions.all { it.isEmpty }) {
                return UseResult.fail(message("collide"))
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