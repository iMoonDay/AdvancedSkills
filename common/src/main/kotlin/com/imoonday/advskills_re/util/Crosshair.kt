package com.imoonday.advskills_re.util

import net.minecraft.util.*

interface Crosshair {

    val texture: Identifier
    val u: Float
    val v: Float
    val width: Int
    val height: Int
    val textureWidth: Int
    val textureHeight: Int

    /**
     * The one with the largest absolute value takes precedence, regardless of positive or negative, and negative numbers take precedence.
     * */
    val priority: Int
}