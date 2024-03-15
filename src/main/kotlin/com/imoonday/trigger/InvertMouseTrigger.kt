package com.imoonday.trigger

interface InvertMouseTrigger : SkillTrigger {

    fun shouldInvertMouse(): Boolean = false

    fun shouldInvertMouseX(): Boolean = shouldInvertMouse()
    fun shouldInvertMouseY(): Boolean = shouldInvertMouse()
}