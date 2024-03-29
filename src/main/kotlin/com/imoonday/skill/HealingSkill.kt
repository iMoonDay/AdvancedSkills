package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.trigger.SynchronousCoolingTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent

abstract class HealingSkill(
    id: String,
    types: List<SkillType>,
    cooldown: Int,
    rarity: Rarity,
    sound: SoundEvent? = ModSounds.HEAL,
    val amount: Float,
) : Skill(id, types, cooldown, rarity, sound), SynchronousCoolingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.heal(amount)
        return UseResult.success()
    }

    override fun getOtherSkills(): Set<Skill> =
        getValidSkills().filter { it is HealingSkill && it != this }.toSet()
}