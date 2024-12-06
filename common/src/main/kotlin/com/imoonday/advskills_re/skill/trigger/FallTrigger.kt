package com.imoonday.advskills_re.skill.trigger

import net.minecraft.server.network.*

interface FallTrigger : SkillTrigger {

    fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int = amount
}