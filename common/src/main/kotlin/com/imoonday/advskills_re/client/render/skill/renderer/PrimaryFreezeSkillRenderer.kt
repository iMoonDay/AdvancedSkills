package com.imoonday.advskills_re.client.render.skill.renderer

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.gui.*
import net.minecraft.util.*

class PrimaryFreezeSkillRenderer : IOverlayRenderer<PrimaryFreezeSkill> {

    override fun render(skill: PrimaryFreezeSkill, drawContext: DrawContext) {
        val player = clientPlayer ?: return
        if (!skill.isInSpecialState(player)) return

        renderOverlay(drawContext)
    }

    private fun renderOverlay(context: DrawContext) {
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        context.drawTexture(
            FROZEN_OVERLAY,
            0,
            0,
            -90,
            0.0f,
            0.0f,
            context.scaledWindowWidth,
            context.scaledWindowHeight,
            context.scaledWindowWidth,
            context.scaledWindowHeight
        )
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
    }

    companion object {

        private val FROZEN_OVERLAY: Identifier = Identifier("textures/misc/powder_snow_outline.png")
    }
}