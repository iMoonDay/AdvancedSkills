package com.imoonday.trigger

import com.imoonday.util.SkillSlot
import net.minecraft.server.network.ServerPlayerEntity

interface EquipTrigger : SkillTrigger {

    fun onEquipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}