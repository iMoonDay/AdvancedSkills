package com.imoonday.skill

import com.imoonday.trigger.LongPressTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent

abstract class LongPressSkill(
    id: String,
    vararg types: SkillType,
    cooldown: Int,
    rarity: Rarity,
    sound: SoundEvent? = null,
) : Skill(id, types = types, cooldown, rarity, sound), LongPressTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = onRelease(user, 1)

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}