package com.imoonday.trigger

import com.imoonday.util.*
import net.minecraft.server.network.*

interface UnequipTrigger : SkillTrigger {

    fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}