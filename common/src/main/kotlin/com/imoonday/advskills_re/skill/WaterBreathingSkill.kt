package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class WaterBreathingSkill : Skill(
    id = "water_breathing",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), AutoStopTrigger, BreatheInWaterTrigger {

    override val persistTime: Int = 30 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
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