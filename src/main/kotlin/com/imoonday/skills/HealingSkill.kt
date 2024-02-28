package com.imoonday.skills

import com.imoonday.init.ModSounds
import com.imoonday.triggers.SynchronousCoolingTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent

abstract class HealingSkill(
    id: String,
    types: Array<SkillType>,
    cooldown: Int,
    rarity: Rarity,
    sound: SoundEvent? = ModSounds.HEAL,
    val amount: Float,
) : Skill(id, types = types, cooldown, rarity, sound), SynchronousCoolingTrigger {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.heal(amount)
        return UseResult.success()
    }

    override val otherSkills: Set<Skill>
        get() = Skills.SKILLS.filter { it is HealingSkill && it != this }.toSet()
}