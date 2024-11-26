package com.imoonday.advskills_re.trigger

interface CameraUpdateMovementTrigger : SkillTrigger {

    fun getDelta(original: Float): Float = original
}