package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
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

    override fun shouldFlashIcon(player: PlayerEntity): Boolean = false
}