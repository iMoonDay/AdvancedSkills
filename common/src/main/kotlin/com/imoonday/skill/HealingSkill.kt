package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import java.util.function.*

abstract class HealingSkill(
    id: String,
    types: List<SkillType>,
    cooldown: Int,
    rarity: Rarity,
    sound: Supplier<SoundEvent>? = ModSounds.HEAL,
    val amount: Float,
) : Skill(id, types, cooldown, rarity, sound), SynchronousCoolingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.heal(amount)
        return UseResult.success()
    }

    override fun getOtherSkills(): Set<Skill> =
        getValidSkills().filter { it is HealingSkill && it != this }.toSet()
}