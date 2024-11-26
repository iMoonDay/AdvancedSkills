package com.imoonday.advskills_re.trigger

interface InvertMouseTrigger : SkillTrigger {

    fun shouldInvertMouse(): Boolean = false

    fun shouldInvertMouseX(): Boolean = shouldInvertMouse()
    fun shouldInvertMouseY(): Boolean = shouldInvertMouse()
}