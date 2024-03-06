package com.imoonday.trigger

import com.imoonday.util.SkillSlot
import net.minecraft.server.network.ServerPlayerEntity

interface UnequipTrigger {

    fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot)
}