package com.imoonday.trigger

import net.minecraft.client.gui.DrawContext

interface HudRenderTrigger : SkillTrigger {

    fun render(context: DrawContext) = Unit
}