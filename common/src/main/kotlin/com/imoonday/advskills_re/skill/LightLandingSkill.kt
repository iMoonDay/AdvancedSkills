package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class LightLandingSkill : Skill(
    Settings(
        id = "light_landing",
        types = listOf(SkillType.DEFENSE, SkillType.UTILITY, SkillType.ENHANCEMENT),
        cooldown = 30,
        rarity = SkillRarity.SUPERB,
    )
), FallTrigger, AutoStopTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_LANDING_SOUND, DEFAULT_LANDING_SOUND)
            .addParameter(PARAM_BREAK_SOUND, DEFAULT_BREAK_SOUND)
            .addParameter(
                name = PARAM_DURATION,
                baseValue = DEFAULT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_DURATION, player, DEFAULT_DURATION, 0)

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        if (!player.isUsing() || amount <= 0) return amount

        player.playSoundFromParam(PARAM_LANDING_SOUND, DEFAULT_LANDING_SOUND)
        player.serverWorld.spawnParticles(ParticleTypes.CLOUD, player.x, player.y, player.z, 10, 0.5, 0.0, 0.5, 0.1)
        return 0
    }

    companion object {

        // Default Values
        private const val DEFAULT_DURATION = 60 * 20 // 1分钟
        private val DEFAULT_LANDING_SOUND = SoundEvents.BLOCK_WOOL_FALL
        private val DEFAULT_BREAK_SOUND = SoundEvents.ITEM_SHIELD_BREAK

        // Parameter Names
        private const val PARAM_LANDING_SOUND = "landing_sound"  // 落地音效
        private const val PARAM_BREAK_SOUND = "break_sound"  // 破碎音效
        private const val PARAM_DURATION = "duration"  // 持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}