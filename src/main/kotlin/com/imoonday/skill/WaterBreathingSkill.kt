package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.BreatheInWaterTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.spawnParticles
import com.imoonday.util.toBlockPos
import net.minecraft.fluid.Fluids
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class WaterBreathingSkill : Skill(
    id = "water_breathing",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = Rarity.RARE,
), AutoStopTrigger, BreatheInWaterTrigger {

    override fun getPersistTime(): Int = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(false)

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        super.onStop(player)
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
        super.tick(player, usedTime)
    }
}