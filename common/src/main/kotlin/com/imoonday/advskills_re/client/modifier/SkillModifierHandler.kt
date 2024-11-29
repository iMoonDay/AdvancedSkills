package com.imoonday.advskills_re.client.modifier

import com.imoonday.advskills_re.skill.*
import net.minecraft.client.util.math.*

object SkillModifierHandler {

    private val gameRendererModifiers: MutableMap<Skill, IGameRendererModifier<Skill>> = mutableMapOf()

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : Skill> addModifier(skill: T, modifier: IModifier<T>) {
        if (modifier is IGameRendererModifier) gameRendererModifiers[skill] = modifier as IGameRendererModifier<Skill>
    }

    fun applyGameRendererModifiers(tickDelta: Float, limitTime: Long, matrices: MatrixStack) {
        gameRendererModifiers.forEach { it.value.modifyWorld(it.key, tickDelta, limitTime, matrices) }
    }

    fun register() {
        addModifier(Skills.REVERSE_GRAVITY, ReverseGravitySkillModifier())
    }
}