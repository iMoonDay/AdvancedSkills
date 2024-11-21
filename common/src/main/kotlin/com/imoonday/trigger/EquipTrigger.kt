package com.imoonday.trigger

import com.imoonday.util.*
import net.minecraft.server.network.*

interface EquipTrigger : SkillTrigger {

    fun onEquipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}