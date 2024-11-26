package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.api.*

interface WorldRendererTrigger : SkillTrigger {

    fun renderAfterEntities(context: WorldRenderContext) = Unit

    fun renderLast(context: WorldRenderContext) = Unit
}