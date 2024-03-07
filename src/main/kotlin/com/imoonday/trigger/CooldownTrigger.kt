package com.imoonday.trigger

interface CooldownTrigger : SkillTrigger {

    fun getCooldown(original: Int): Int = original
}