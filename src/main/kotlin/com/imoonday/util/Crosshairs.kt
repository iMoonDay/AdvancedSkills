package com.imoonday.util

import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier

enum class Crosshairs(
    override val priority: Int,
    override val u: Float,
    override val v: Float = 0f,
    override val width: Int = 15,
    override val height: Int = 15,
    override val textureWidth: Int = 256,
    override val textureHeight: Int = 256,
) : Crosshair {

    NONE(-1, 0f),
    CROSS(0, 0f),
    CIRCLE(1, 16f),
    SQUARE(1, 32f),
    BOX(-1, 48f),
    RING(-1, 64f);

    override val texture = id("crosshairs.png")
}

interface Crosshair {

    val texture: Identifier
    val u: Float
    val v: Float
    val width: Int
    val height: Int
    val textureWidth: Int
    val textureHeight: Int
    val priority: Int

    fun draw(context: DrawContext) {
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