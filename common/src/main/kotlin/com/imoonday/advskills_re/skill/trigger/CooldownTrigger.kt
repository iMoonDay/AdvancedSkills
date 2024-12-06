package com.imoonday.advskills_re.skill.trigger

interface CooldownTrigger : SkillTrigger {

    fun getCooldown(original: Int): Int = original
}