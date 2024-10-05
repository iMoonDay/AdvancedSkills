package com.imoonday.trigger

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack

interface WorldRenderTrigger : SkillTrigger {

    fun apply(matrixStack: MatrixStack, tickDelta: Float, client: MinecraftClient) = Unit
}