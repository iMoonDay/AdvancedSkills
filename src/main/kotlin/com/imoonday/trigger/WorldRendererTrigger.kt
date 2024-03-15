package com.imoonday.trigger

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

interface WorldRendererTrigger : SkillTrigger {

    fun render(context: WorldRenderContext) = Unit
}