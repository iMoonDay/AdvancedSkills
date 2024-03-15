package com.imoonday.trigger

interface CameraUpdateMovementTrigger : SkillTrigger {

    fun getDelta(original: Float): Float = original
}