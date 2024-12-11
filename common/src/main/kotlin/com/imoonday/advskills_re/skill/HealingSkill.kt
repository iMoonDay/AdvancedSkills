package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import java.util.function.*

abstract class HealingSkill(
    id: String,
    types: List<SkillType>,
    cooldown: Int,
    rarity: SkillRarity,
    sound: Supplier<SoundEvent>? = ModSounds.HEAL,
    val amount: Float,
    enhancements: Set<SkillEnhancementType<*>> = setOf(SkillEnhancements.HEALING_AMOUNT)
) : Skill(id, types, cooldown, rarity, sound, enhancements), SynchronousCoolingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        val healingAmount = getEnhancedValue(user, SkillEnhancements.HEALING_AMOUNT, amount)
        user.heal(healingAmount)
        user.spawnParticles(
            ParticleTypes.HEART,
            false, user.centerPos, healingAmount.toInt(),
            0.5, 0.5, 0.5, 0.1
        )
        return UseResult.success()
    }

    override fun getOtherSkills(player: PlayerEntity): Set<Skill> =
        player.learnedSkills.filter { it is HealingSkill && it != this }.toSet()
}