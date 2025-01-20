package com.imoonday.advskills_re.util

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

    override val texture = id("textures/gui/crosshairs.png")
}

