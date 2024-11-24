package com.imoonday.client.screen.component

import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.widget.*
import net.minecraft.util.*

class ButtonIconWidget : IconWidget {

    protected val actions: MutableMap<Int, (ButtonIconWidget) -> Unit> = mutableMapOf()
    val hoveredTexture: Identifier?
    var textureU: Float = 0f
    var textureV: Float = 0f
    var hoveredTextureU: Float = 0f
    var hoveredTextureV: Float = 0f
    var textureWidth: Int
    var textureHeight: Int

    constructor(width: Int, height: Int, texture: Identifier, hoveredTexture: Identifier?) : super(
        width,
        height,
        texture
    ) {
        this.hoveredTexture = hoveredTexture
        this.textureWidth = width
        this.textureHeight = height
    }

    constructor(x: Int, y: Int, width: Int, height: Int, texture: Identifier, hoveredTexture: Identifier?) : super(
        x,
        y,
        width,
        height,
        texture
    ) {
        this.hoveredTexture = hoveredTexture
        this.textureWidth = width
        this.textureHeight = height
    }

    constructor(x: Int, y: Int, width: Int, height: Int, texture: Identifier) : this(x, y, width, height, texture, null)

    fun setTextureU(textureU: Float): ButtonIconWidget {
        this.textureU = textureU
        return this
    }

    fun setTextureV(textureV: Float): ButtonIconWidget {
        this.textureV = textureV
        return this
    }

    fun setTextureOffset(u: Float, v: Float): ButtonIconWidget {
        this.textureU = u
        this.textureV = v
        return this
    }

    fun setHoveredTextureU(hoveredTextureU: Float): ButtonIconWidget {
        this.hoveredTextureU = hoveredTextureU
        return this
    }

    fun setHoveredTextureV(hoveredTextureV: Float): ButtonIconWidget {
        this.hoveredTextureV = hoveredTextureV
        return this
    }

    fun setHoveredTextureOffset(u: Float, v: Float): ButtonIconWidget {
        this.hoveredTextureU = u
        this.hoveredTextureV = v
        return this
    }

    fun setTextureWidth(textureWidth: Int): ButtonIconWidget {
        this.textureWidth = textureWidth
        return this
    }

    fun setTextureHeight(textureHeight: Int): ButtonIconWidget {
        this.textureHeight = textureHeight
        return this
    }

    fun setTextureSize(width: Int, height: Int): ButtonIconWidget {
        this.textureWidth = width
        this.textureHeight = height
        return this
    }

    fun setTextureSize(size: Int): ButtonIconWidget {
        this.setTextureSize(size, size)
        return this
    }

    fun addClickAction(button: Int, action: (ButtonIconWidget) -> Unit): ButtonIconWidget {
        actions[button] = action
        return this
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!this.active || !this.visible) {
            return false
        }
        if (this.clicked(mouseX, mouseY)) {
            val action = actions[button]
            if (action != null) {
                this.playDownSound(MinecraftClient.getInstance().soundManager)
                action(this)
                return true
            }
        }
        return false
    }

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val i = this.getWidth()
        val j = this.getHeight()
        val x = this.x
        val y = this.y
        val textureWidth = textureWidth
        val textureHeight = textureHeight
        val hoveredTexture = this.hoveredTexture
        if (hoveredTexture != null && this.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            context.drawTexture(
                hoveredTexture,
                x,
                y,
                hoveredTextureU,
                hoveredTextureV,
                i,
                j,
                textureWidth,
                textureHeight
            )
        } else {
            context.drawTexture(this.texture, x, y, textureU, textureV, i, j, textureWidth, textureHeight)
        }
    }
}
