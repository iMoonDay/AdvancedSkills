package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*

class ArrowRainSkill : Skill(
    id = "arrow_rain",
    types = listOf(SkillType.ATTACK, SkillType.SUMMON),
    cooldown = 15,
    rarity = SkillRarity.RARE,
    sound = SoundEvents::ENTITY_ARROW_SHOOT,
    enhancements = setOf(
        SkillEnhancements.DAMAGE,
        SkillEnhancements.LAUNCH_COUNT,
        SkillEnhancements.RANGE,
        SkillEnhancements.SUMMON_AMOUNT
    )
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val enhancement = user.getEnhancement(SkillEnhancements.DAMAGE)
        val damageModifier: ((Double) -> Double)? = if (enhancement == null) null
        else { value -> enhancement.applyMultiplier(value) }

        val raycast = user.raycast(256.0, 0f, true)
        val center = if (raycast.type == HitResult.Type.MISS) user.pos else raycast.pos
        val remainingTimes = 4 + user.getEnhancementLvl(SkillEnhancements.LAUNCH_COUNT)
        val range = user.getEnhancementLvl(SkillEnhancements.RANGE) * 2.0
        val amount = user.getEnhancementLvl(SkillEnhancements.SUMMON_AMOUNT) * 10
        user.executeAndAddTask(2, remainingTimes) { spawnArrows(user, center, damageModifier, range, amount) }
        return UseResult.success()
    }

    private fun spawnArrows(
        user: ServerPlayerEntity,
        center: Vec3d,
        damageModifier: ((Double) -> Double)?,
        extraRange: Double,
        extraAmount: Int
    ): Boolean {
        val random = user.random
        val amount = random.nextInt(51) + 50 + extraAmount
        val range = (amount - extraAmount) * 0.25 + extraRange

        for (j in 0 until amount) {
            val result = user.world.spawnEntity(
                UngroundedArrowEntity(
                    user.world,
                    center.x + random.nextDouble() * range - range / 2,
                    center.y + 20,
                    center.z + random.nextDouble() * range - range / 2,
                    user
                ).apply {
                    pitch = -90f
                    damageModifier?.let { damage = it(damage) }
                }.also {
                    user.spawnParticles(ParticleTypes.CLOUD, false, it.pos, 5, 1.0, 0.0, 1.0, 0.0)
                })
            if (!result) return false
        }
        return true
    }
}