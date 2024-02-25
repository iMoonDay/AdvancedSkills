package com.imoonday.trigger

import com.imoonday.utils.SkillSlot
import net.minecraft.server.network.ServerPlayerEntity

interface EquipTrigger {

    fun onEquipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot)
}