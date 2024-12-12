package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import kotlin.math.*

class RisingShockSkill : Skill(
    id = "rising_shock",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 10,
    rarity = SkillRarity.RARE,
    sound = ModSounds.DASH,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), AutoStopTrigger, GravityTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.stopFallFlying()
        user.velocity = Vec3d(0.0, max(user.velocity.y, 0.5), 0.0)
        user.updateVelocity()
        return UseResult.startUsing(user, this)
    }

    override val persistTime: Int = 8

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        player.velocity = Vec3d(0.0, max(player.velocity.y, 0.5), 0.0)
        player.updateVelocity()
        player.spawnParticles(
            ParticleTypes.CLOUD,
            false,
            Vec3d(player.x, player.boundingBox.minY, player.z),
            10,
            0.5,
            0.5,
            0.5,
            0.1
        )
        player.world.getNonSpectatingEntities(LivingEntity::class.java, player.boundingBox.expand(1.0)).forEach {
            it.velocity = it.velocity.withAxis(Direction.Axis.Y, max(it.velocity.y, 0.5))
            it.velocityDirty = true
            (it as? ServerPlayerEntity)?.updateVelocity()
        }
        super.serverTick(player, usedTime)
    }
}