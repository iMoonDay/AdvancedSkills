package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import java.util.function.*

abstract class LongPressSkill(
    id: String,
    types: List<SkillType>,
    cooldown: Int,
    rarity: Rarity,
    sound: Supplier<SoundEvent>? = null,
) : Skill(id, types, cooldown, rarity, sound), LongPressTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = onRelease(user, 1)

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit

    override fun shouldFlashIcon(): Boolean = false
}