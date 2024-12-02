package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
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
    rarity: Rarity,
    sound: Supplier<SoundEvent>? = ModSounds.HEAL,
    val amount: Float,
) : Skill(id, types, cooldown, rarity, sound), SynchronousCoolingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.heal(amount)
        user.spawnParticles(
            ParticleTypes.HEART,
            false, user.centerPos, amount.toInt(),
            0.5, 0.5, 0.5, 0.1
        )
        return UseResult.success()
    }

    override fun getOtherSkills(player: PlayerEntity): Set<Skill> =
        Skills.getValidSkills().filter { it is HealingSkill && it != this }.toSet()
}