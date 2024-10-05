package com.imoonday.trigger

import com.imoonday.advanced_skills_re.api.*

interface WorldRendererTrigger : SkillTrigger {

    fun renderAfterEntities(context: WorldRenderContext) = Unit

    fun renderLast(context: WorldRenderContext) = Unit
}