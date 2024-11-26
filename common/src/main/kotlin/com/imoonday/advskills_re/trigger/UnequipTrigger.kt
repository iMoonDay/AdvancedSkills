package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

interface UnequipTrigger : SkillTrigger {

    fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}