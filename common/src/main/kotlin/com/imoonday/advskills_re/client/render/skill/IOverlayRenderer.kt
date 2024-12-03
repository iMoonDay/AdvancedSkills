package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.gui.*

interface IOverlayRenderer<T : Skill> : IRenderer<T> {

    fun render(skill: T, drawContext: DrawContext)
}