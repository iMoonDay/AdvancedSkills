package com.imoonday.advskills_re.skill.trigger

import net.minecraft.server.network.*

interface DangerTrigger : SkillTrigger {

    fun isDangerousTo(player: ServerPlayerEntity, other: ServerPlayerEntity): Boolean = player.isUsing()
}