package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import net.minecraft.server.network.*

interface UnequipTrigger : SkillTrigger {

    fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = true

    fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit
}