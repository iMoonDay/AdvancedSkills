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
            name = PARAM_BREATH_DURATION,
            baseValue = DEFAULT_BREATH_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT,
            genericText = true
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_BREATH_DURATION, player, DEFAULT_BREATH_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        val shouldAddBubble = (player.isUsing()
            && usedTime % BUBBLE_INTERVAL == 0
            && player.world.getFluidState(player.eyePos.toBlockPos()).isOf(Fluids.WATER))
        if (shouldAddBubble) {
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

    companion object {
        // Default Values
        private const val DEFAULT_BREATH_DURATION = 30 * 20
        private const val BUBBLE_INTERVAL = 4  // 气泡生成间隔

        // Parameter Names
        private const val PARAM_BREATH_DURATION = "breath_duration"  // 水下呼吸持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}