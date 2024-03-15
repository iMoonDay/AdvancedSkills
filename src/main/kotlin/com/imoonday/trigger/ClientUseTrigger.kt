package com.imoonday.trigger

interface ClientUseTrigger : SkillTrigger {

    fun onUse() = Unit

    fun onStop() = Unit
}