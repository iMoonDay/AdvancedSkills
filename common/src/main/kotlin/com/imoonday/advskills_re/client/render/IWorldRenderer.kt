package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.skill.*

interface IWorldRenderer<T : Skill> : IRenderer<T> {

    fun renderAfterEntities(skill: T, context: WorldRenderContext) = Unit

    fun renderLast(skill: T, context: WorldRenderContext) = Unit
}