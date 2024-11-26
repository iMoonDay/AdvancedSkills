package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

interface EquipTrigger : SkillTrigger {

    fun onEquipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}