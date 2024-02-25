package com.imoonday.skills

import com.imoonday.components.getSkillData
import com.imoonday.components.isUsingSkill
import com.imoonday.components.startUsingSkill
import com.imoonday.components.stopUsingSkill
import com.imoonday.init.ModSounds
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.send
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

class PiercingSkill : Skill(
    id = "piercing",
    types = arrayOf(SkillType.MOVEMENT, SkillType.ATTACK),
    cooldown = 15,
    rarity = Rarity.VERY_RARE,
    sound = ModSounds.PIERCING
), AutoStopTrigger {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.velocityDirty = true
        user.velocity = user.rotationVector.normalize().multiply(1.5, 0.0, 1.5)
        val noGravity = user.hasNoGravity()
        user.setNoGravity(true)
        user.send(EntityVelocityUpdateS2CPacket(user))
        return UseResult.of(user.startUsingSkill(skill, NbtCompound().apply {
            putDouble("x", user.velocity.x)
            putDouble("z", user.velocity.z)
            putBoolean("noGravity", noGravity)
        }))
    }

    override val persistTime: Int = 8
    override val skill: Skill
        get() = this

    override fun onStop(player: ServerPlayerEntity) {
        player.velocityDirty = true
        player.velocity = Vec3d.ZERO
        player.getSkillData(skill)?.let {
            player.setNoGravity(it.getBoolean("noGravity"))
        }
        player.send(EntityVelocityUpdateS2CPacket(player))
        super.onStop(player)
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsingSkill(this)) return
        if (player.horizontalCollision) {
            onStop(player)
            player.stopUsingSkill(skill)
            return
        }
        player.getSkillData(skill)?.let {
            if (it.contains("x") && it.contains("z")) {
                player.velocityDirty = true
                player.velocity = Vec3d(it.getDouble("x"), 0.0, it.getDouble("z"))
                player.send(EntityVelocityUpdateS2CPacket(player))
            }
        }
        player.world.getOtherEntities(player, player.boundingBox) { it.isLiving && it.isAlive }.forEach {
            it.damage(player.damageSources.playerAttack(player), 6.0f)
            it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(1.5))
        }
        super.tick(player, usedTime)
    }
}