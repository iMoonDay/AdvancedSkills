package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*

class ArrowRainSkill : Skill(
    Settings(
        id = "arrow_rain",
        types = listOf(SkillType.ATTACK, SkillType.SUMMON),
        cooldown = 15,
        rarity = SkillRarity.RARE
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_MAX_DISTANCE, DEFAULT_MAX_DISTANCE)
            .addParameter(PARAM_MIN_ARROWS, DEFAULT_MIN_ARROWS)
            .addParameter(PARAM_MAX_ARROWS, DEFAULT_MAX_ARROWS)
            .addParameter(PARAM_WAVE_INTERVAL, DEFAULT_WAVE_INTERVAL)
            .addParameter(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND)
            .addParameter(
                name = PARAM_ARROW_DAMAGE,
                baseValue = DEFAULT_ARROW_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_WAVE_COUNT,
                baseValue = DEFAULT_WAVE_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_SUMMON_RANGE,
                baseValue = DEFAULT_SUMMON_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 2.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_EXTRA_ARROWS,
                baseValue = DEFAULT_EXTRA_ARROWS,
                enhancementId = ENHANCEMENT_AMOUNT,
                value = 10,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val damage = getDoubleParam(PARAM_ARROW_DAMAGE, user, DEFAULT_ARROW_DAMAGE)
        val maxDistance = getDoubleParam(PARAM_MAX_DISTANCE, user, DEFAULT_MAX_DISTANCE)
        val raycast = user.raycast(maxDistance, 0f, true)
        val center = if (raycast.type == HitResult.Type.MISS) user.pos else raycast.pos
        val waveCount = getIntParam(PARAM_WAVE_COUNT, user, DEFAULT_WAVE_COUNT)
        val range = getDoubleParam(PARAM_SUMMON_RANGE, user, DEFAULT_SUMMON_RANGE)
        val extraArrows = getIntParam(PARAM_EXTRA_ARROWS, user, DEFAULT_EXTRA_ARROWS)
        val minAmount = getIntParam(PARAM_MIN_ARROWS, user, DEFAULT_MIN_ARROWS)
        val maxAmount = getIntParam(PARAM_MAX_ARROWS, user, DEFAULT_MAX_ARROWS)
        val interval = getIntParam(PARAM_WAVE_INTERVAL, user, DEFAULT_WAVE_INTERVAL)
        val sound = getSoundEventParam(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND)
        user.executeAndAddTask(interval, waveCount) {
            spawnArrows(user, center, damage, range, minAmount, maxAmount, extraArrows, sound)
        }
        return UseResult.success()
    }

    private fun spawnArrows(
        user: ServerPlayerEntity,
        center: Vec3d,
        damage: Double,
        range: Double,
        min: Int,
        max: Int,
        extraAmount: Int,
        sound: SoundEvent?
    ): Boolean {
        val random = user.random
        val amount = random.nextBetween(min, max) + extraAmount

        sound?.let { user.serverWorld.playSound(null, center.x, center.y, center.z, it, SoundCategory.VOICE, 1f, 1f) }
        var result = false
        val particles: MutableList<ParticleS2CPacket> = mutableListOf()
        repeat(amount) {
            user.world.spawnEntity(
                UngroundedArrowEntity(
                    user.world,
                    center.x + random.nextDouble() * range - range / 2,
                    center.y + 20,
                    center.z + random.nextDouble() * range - range / 2,
                    user
                ).apply {
                    pitch = -90f
                    this.damage = damage
                }.also {
                    particles.add(
                        ParticleS2CPacket(
                            ParticleTypes.CLOUD,
                            false,
                            it.x, it.y, it.z,
                            1f, 0f, 1f,
                            0f, 5
                        )
                    )
                }
            ).also {
                if (it && !result) result = true
            }
        }

        if (particles.isNotEmpty()) {
            user.sendPacket(BundleS2CPacket(particles))
        }
        return result
    }

    companion object {

        // Default Values
        private const val DEFAULT_MAX_DISTANCE = 256.0
        private const val DEFAULT_MIN_ARROWS = 50
        private const val DEFAULT_MAX_ARROWS = 100
        private const val DEFAULT_WAVE_INTERVAL = 2
        private const val DEFAULT_ARROW_DAMAGE = 2.0
        private const val DEFAULT_WAVE_COUNT = 5
        private const val DEFAULT_SUMMON_RANGE = 20.0
        private const val DEFAULT_EXTRA_ARROWS = 0
        private val DEFAULT_LAUNCH_SOUND = SoundEvents.ENTITY_ARROW_SHOOT

        // Parameter Names
        private const val PARAM_MAX_DISTANCE = "maximum_distance"  // 最大射程
        private const val PARAM_MIN_ARROWS = "minimum_arrows"  // 最小箭矢数量
        private const val PARAM_MAX_ARROWS = "maximum_arrows"  // 最大箭矢数量
        private const val PARAM_WAVE_INTERVAL = "wave_interval"  // 波次间隔
        private const val PARAM_LAUNCH_SOUND = "launch_sound"  // 发射音效
        private const val PARAM_ARROW_DAMAGE = "arrow_damage"  // 箭矢伤害
        private const val PARAM_WAVE_COUNT = "wave_count"  // 波次数量
        private const val PARAM_SUMMON_RANGE = "summon_range"  // 召唤范围
        private const val PARAM_EXTRA_ARROWS = "extra_arrows"  // 额外箭矢数量

        // Enhancement IDs
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应箭矢伤害
        private const val ENHANCEMENT_COUNT = "count"  // 对应波次数量
        private const val ENHANCEMENT_RANGE = "range"  // 对应召唤范围
        private const val ENHANCEMENT_AMOUNT = "amount"  // 对应额外箭矢
    }
}