package com.imoonday.advskills_re.client.render.skill.special

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*

open class CrosshairRenderer<T> : ICrosshairRenderer<T> where T : Skill, T : CrosshairTrigger {

    override fun render(skill: T, context: DrawContext) {
        val player = clientPlayer ?: return

        skill.getCrosshair(player).draw(context)
    }

    override fun shouldRenderCrosshair(skill: T): Boolean {
        val player = clientPlayer ?: return false
        return skill.shouldRenderCrosshair(player)
    }

    override fun getPriority(skill: T): Int {
        val player = clientPlayer ?: return 0
        return skill.getPriority(player)
    }

    fun Crosshair.draw(context: DrawContext) {
        if (this == Crosshairs.NONE) return
        context.drawTexture(
            texture,
            (context.scaledWindowWidth - width) / 2,
            (context.scaledWindowHeight - height) / 2,
            u,
            v,
            width,
            height,
            textureWidth,
            textureHeight
        )
    }
}