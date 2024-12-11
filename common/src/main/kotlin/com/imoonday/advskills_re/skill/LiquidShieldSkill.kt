package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.particle.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class LiquidShieldSkill : Skill(
    id = "liquid_shield",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 60,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), TickTrigger, AutoStopTrigger, FluidMovementTrigger, BreatheInWaterTrigger {

    override val persistTime: Int = 15 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.playSound(SoundEvents.BLOCK_WATER_AMBIENT)
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        if (player.isUsing()
            && usedTime % 4 == 0
            && player.world.getFluidState(player.eyePos.toBlockPos()).isOf(Fluids.WATER)
        ) {
            val rotation = player.rotationVector.normalize().multiply(player.width / 2.0)
            player.world.addParticle(
                ParticleTypes.BUBBLE,
                player.x + rotation.x,
                player.eyeY + rotation.y,
                player.z + rotation.z,
                0.0,
                1.0,
                0.0,
            )
        }
        super<AutoStopTrigger>.clientTick(player, usedTime)
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean = player.isUsing()

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (player.isUsing()) 0.0 else speed

    override fun canBreatheInWater(player: PlayerEntity): Boolean = player.isUsing()

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}