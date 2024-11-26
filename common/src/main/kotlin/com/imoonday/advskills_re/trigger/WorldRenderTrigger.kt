package com.imoonday.advskills_re.trigger

import net.minecraft.client.*
import net.minecraft.client.util.math.*

interface WorldRenderTrigger : SkillTrigger {

    fun apply(matrixStack: MatrixStack, tickDelta: Float, client: MinecraftClient) = Unit
}