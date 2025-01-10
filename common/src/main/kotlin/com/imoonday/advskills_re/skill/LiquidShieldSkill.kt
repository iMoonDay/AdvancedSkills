package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
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
    Settings(
        id = "liquid_shield",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 60,
        rarity = SkillRarity.SUPERB
    )
), TickTrigger, AutoStopTrigger, FluidMovementTrigger, BreatheInWaterTrigger {

    init {
        settings
            .addParameter(PARAM_SHIELD_SOUND, DEFAULT_SHIELD_SOUND)
            .addParameter(
                name = PARAM_SHIELD_DURATION,
                baseValue = DEFAULT_SHIELD_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.playSoundFromParam(PARAM_SHIELD_SOUND, DEFAULT_SHIELD_SOUND)
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

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_SHIELD_DURATION, player, DEFAULT_SHIELD_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {

        // Default Values
        private const val DEFAULT_SHIELD_DURATION = 15 * 20
        private val DEFAULT_SHIELD_SOUND = SoundEvents.BLOCK_WATER_AMBIENT

        // Parameter Names
        private const val PARAM_SHIELD_SOUND = "shield_sound"  // 护盾音效
        private const val PARAM_SHIELD_DURATION = "shield_duration"  // 护盾持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}