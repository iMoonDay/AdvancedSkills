package com.imoonday.skill

import com.imoonday.util.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction

class TeleportSkill : Skill(
    id = "teleport",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 2,
    rarity = Rarity.UNCOMMON
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            val offset = rotationVector.withAxis(Direction.Axis.Y, 0.0).normalize().multiply(2.0)
            val collisions = world.getBlockCollisions(this, boundingBox.offset(offset))
            if (!collisions.all { it.isEmpty }) {
                return UseResult.fail(translateSkill(this@TeleportSkill.id.path, "collide"))
            }
            val velocity = velocity
            val prevPos = pos
            requestTeleportOffset(offset.x, offset.y, offset.z)
            this.velocity = velocity
            send(EntityVelocityUpdateS2CPacket(this))
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
                prevPos.x,
                prevPos.y + height / 2.0,
                prevPos.z,
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