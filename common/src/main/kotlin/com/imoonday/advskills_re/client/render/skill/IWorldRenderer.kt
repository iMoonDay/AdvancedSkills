package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.skill.*

interface IWorldRenderer<T : Skill> : com.imoonday.advskills_re.client.render.skill.IRenderer<T> {

    fun renderAfterEntities(skill: T, context: WorldRenderContext) = Unit

    fun renderLast(skill: T, context: WorldRenderContext) = Unit
}