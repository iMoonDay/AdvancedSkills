package com.imoonday.advskills_re.client.modifier

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.util.math.*

interface IGameRendererModifier<T : Skill> : IModifier<T> {

    fun modifyWorld(skill: T, tickDelta: Float, limitTime: Long, matrices: MatrixStack)
}