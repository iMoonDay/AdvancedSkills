package com.imoonday.advskills_re.trigger

interface CooldownTrigger : SkillTrigger {

    fun getCooldown(original: Int): Int = original
}