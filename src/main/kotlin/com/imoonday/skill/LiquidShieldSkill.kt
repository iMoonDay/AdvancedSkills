package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.BreatheInWaterTrigger
import com.imoonday.trigger.FluidMovementTrigger
import com.imoonday.trigger.TickTrigger
import com.imoonday.util.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class LiquidShieldSkill : Skill(
    id = "liquid_shield",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 60,
    rarity = Rarity.SUPERB,
    sound = SoundEvents.BLOCK_WATER_AMBIENT
), TickTrigger, AutoStopTrigger, FluidMovementTrigger, BreatheInWaterTrigger {

    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult {
        val active = user.toggleUsing()
        if (!active) user.startCooling()
        return UseResult.consume(translateActive(active, name.string))
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing()
            && usedTime % 4 == 0
            && player.world.getFluidState(player.eyePos.toBlockPos()).isOf(Fluids.WATER)
        ) player.spawnParticles(
            ParticleTypes.BUBBLE,
            player.x,
            player.eyeY,
            player.z,
            0,
            0.0,
            1.0,
            0.0,
            1.0
        )
        super<AutoStopTrigger>.tick(player, usedTime)
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean = player.isUsing()

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsing()) speed else 0.0

    override fun canBreatheInWater(player: PlayerEntity): Boolean = player.isUsing()
}