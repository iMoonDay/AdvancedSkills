package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.gui.*

interface ICrosshairRenderer<T : Skill> : IRenderer<T> {

    fun render(skill: T, context: DrawContext)

    fun shouldRenderCrosshair(skill: T): Boolean

    fun getPriority(skill: T): Int
}

