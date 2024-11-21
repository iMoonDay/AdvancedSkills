package com.imoonday.trigger

import com.imoonday.util.*
import net.minecraft.client.gui.*

interface CrosshairTrigger : SkillTrigger {

    fun render(context: DrawContext) {
        if (shouldRender()) getCrosshair().draw(context)
    }

    fun shouldRender(): Boolean = getCrosshair() != Crosshairs.NONE

    fun getCrosshair(): Crosshair = Crosshairs.NONE

    fun getPriority(): Int = getCrosshair().priority
}