package com.imoonday.skills

import com.imoonday.triggers.LongPressTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent

abstract class LongPressSkill(
    id: String,
    vararg types: SkillType,
    cooldown: Int,
    rarity: Rarity,
    sound: SoundEvent? = null,
) : Skill(id, types = types, cooldown, rarity, sound), LongPressTrigger {

    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = onRelease(user, 1)
}