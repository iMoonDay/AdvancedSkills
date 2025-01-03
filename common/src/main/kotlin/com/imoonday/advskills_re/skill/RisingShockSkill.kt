package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import kotlin.math.*

class RisingShockSkill : Skill(
    Settings(
        id = "rising_shock",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 10,
        rarity = SkillRarity.RARE
    )
), AutoStopTrigger, GravityTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("use_sound", ModSounds.DASH)
            .addParameter(
                name = "persist_time",
                baseValue = 8,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "range",
                baseValue = 1.0,
                enhancementId = "range",
                value = 0.8,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = "velocity_multiplier",
                baseValue = 1.0,
                enhancementId = "velocity",
                value = 0.01,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this) {
        user.stopFallFlying()
        user.velocity = Vec3d(0.0, user.getVelocityY(), 0.0)
        user.updateVelocity()
    }.withSound(getSoundEventParam("use_sound", ModSounds.DASH.get()))

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("persist_time", player, 8, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        val velocityY = player.getVelocityY()
        player.velocity = Vec3d(0.0, velocityY, 0.0)
        player.updateVelocity()
        player.spawnParticles(
            ParticleTypes.CLOUD, false, Vec3d(player.x, player.boundingBox.minY, player.z), 10, 0.5, 0.5, 0.5, 0.1
        )
        val range = getDoubleParam("range", player, 1.0)
        player.world.getOtherEntities(player, player.boundingBox.expand(range)) { it is LivingEntity }.forEach {
            it.velocity = it.velocity.withAxis(Direction.Axis.Y, max(it.velocity.y, velocityY))
            it.velocityDirty = true
            (it as? ServerPlayerEntity)?.updateVelocity()
        }
        super.serverTick(player, usedTime)
    }

    private fun PlayerEntity.getVelocityY(): Double {
        val multiplier = getDoubleParam("velocity_multiplier", this, 1.0)
        return max(velocity.y, 0.5) * multiplier
    }
}