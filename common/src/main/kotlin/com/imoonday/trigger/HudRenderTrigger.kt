package com.imoonday.trigger

import net.minecraft.client.gui.*

interface HudRenderTrigger : SkillTrigger {

    fun render(context: DrawContext) = Unit
}