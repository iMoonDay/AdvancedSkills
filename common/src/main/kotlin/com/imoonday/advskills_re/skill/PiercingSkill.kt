package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class PiercingSkill : Skill(
    Settings(
        id = "piercing",
        types = listOf(SkillType.MOVEMENT, SkillType.ATTACK),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), AutoStopTrigger, DangerTrigger, GravityTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("piercing_sound", ModSounds.PIERCING)
            .addParameter(
                name = timeParamName,
                baseValue = 8,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage",
                baseValue = 6.0f,
                enhancementId = "damage",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "velocity",
                baseValue = 1.5,
                enhancementId = "velocity",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult =
        UseResult.startUsing(user, this, NbtCompound().apply {
            putDouble("x", user.velocity.x)
            putDouble("z", user.velocity.z)
        }) {
            user.stopFallFlying()
            val initialVelocity = getDoubleParam("velocity", user, 1.5)
            user.velocity = user.horizontalRotationVector.normalize().multiply(initialVelocity, 0.0, initialVelocity)
            user.updateVelocity()
        }

    override fun onStop(player: ServerPlayerEntity) {
        player.velocity = Vec3d.ZERO
        player.updateVelocity()
        super.onStop(player)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (player.horizontalCollision) {
            onStop(player)
            player.stopUsing()
            return
        }

        val data = player.getActiveData()
        if (data.contains("x") && data.contains("z")) {
            player.velocity = Vec3d(data.getDouble("x"), 0.0, data.getDouble("z"))
            player.updateVelocity()
        }

        val damage = getFloatParam("damage", player, 6.0f)
        val velocity = getDoubleParam("velocity", player, 1.5)
        player.world.getOtherEntities(
            player, player.boundingBox
        ) { it is LivingEntity }.forEach {
            it.damage(player.damageSources.playerAttack(player), damage)
            it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(velocity).withAxis(Direction.Axis.Y, 1.0))
            it.velocityDirty = true
            (it as? ServerPlayerEntity)?.updateVelocity()
        }
        super.serverTick(player, usedTime)
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (!player.isUsing()) return
        val pos = player.pos
        val halfHeight = player.height / 2.0
        val random = player.random
        for (i in 0 until 20) {
            player.world.addParticle(
                DustParticleEffect.DEFAULT,
                pos.x + random.nextDouble() - 0.5, pos.y + halfHeight, pos.z + random.nextDouble() - 0.5,
                0.0, 0.0, 0.0
            )
        }
    }
}