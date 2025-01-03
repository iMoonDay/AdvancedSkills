package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class WaterBreathingSkill : Skill(
    Settings(
        id = "water_breathing",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 10,
        rarity = SkillRarity.RARE
    )
), AutoStopTrigger, BreatheInWaterTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "persist_time",
            baseValue = 30 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("persist_time", player, 30 * 20, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        val shouldAdd = (player.isUsing()
            && usedTime % 4 == 0
            && player.world.getFluidState(player.eyePos.toBlockPos()).isOf(Fluids.WATER))
        if (shouldAdd) {
            val rotation = player.rotationVector.normalize().multiply(player.width / 2.0)
            player.world.addParticle(
                ParticleTypes.BUBBLE,
                player.x + rotation.x,
                player.eyeY + rotation.y,
                player.z + rotation.z,
                0.0, 1.0, 0.0,
            )
        }
        super.clientTick(player, usedTime)
    }
}