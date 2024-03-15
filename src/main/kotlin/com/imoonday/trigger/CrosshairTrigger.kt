package com.imoonday.trigger

import com.imoonday.util.Crosshair
import com.imoonday.util.Crosshairs
import net.minecraft.client.gui.DrawContext

interface CrosshairTrigger : SkillTrigger {

    fun render(context: DrawContext) {
        if (shouldRender()) getCrosshair().draw(context)
    }

    fun shouldRender(): Boolean = getCrosshair() != Crosshairs.NONE

    fun getCrosshair(): Crosshair = Crosshairs.NONE

    fun getPriority(): Int = getCrosshair().priority
}