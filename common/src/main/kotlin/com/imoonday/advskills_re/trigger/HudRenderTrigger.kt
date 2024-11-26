package com.imoonday.advskills_re.trigger

import net.minecraft.client.gui.*

interface HudRenderTrigger : SkillTrigger {

    fun render(context: DrawContext) = Unit
}