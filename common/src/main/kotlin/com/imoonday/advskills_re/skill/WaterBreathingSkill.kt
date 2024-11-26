package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.toBlockPos
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class WaterBreathingSkill : Skill(
    id = "water_breathing",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = Rarity.RARE,
), AutoStopTrigger, BreatheInWaterTrigger {

    override val persistTime: Int = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(false)

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        super.onStop(player)
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
        super.clientTick(player, usedTime)
    }
}