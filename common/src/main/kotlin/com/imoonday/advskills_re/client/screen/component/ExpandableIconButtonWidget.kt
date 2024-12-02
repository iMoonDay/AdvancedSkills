package com.imoonday.advskills_re.client.screen.component

import net.minecraft.client.font.*
import net.minecraft.client.gui.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.util.math.*

class ExpandableIconButtonWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    texture: Identifier,
    hoveredTexture: Identifier? = null,
    var animationTime: Long = 250,
    var expandDelay: Long = 100,
) : ButtonIconWidget(x, y, width, height, texture, hoveredTexture) {

    private var maxXOffset: Int = width - 4
    private var xOffset: Int = maxXOffset
    private var startAnimationTime: Long? = null
    private var isExpanded: Boolean = false
    private var isExpanding: Boolean = false
    private var isFolding: Boolean = false
    private var textRenderer: TextRenderer? = null
    private var textSupplier: (() -> Text)? = null

    override fun clicked(mouseX: Double, mouseY: Double): Boolean = isMouseOver(mouseX, mouseY)

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
        this.active && this.visible && mouseX >= this.x + this.xOffset && mouseX <= this.x + this.width && mouseY.toInt() in this.y..this.y + this.height

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val currentTime = System.currentTimeMillis()

        this.hovered =
            mouseX >= this.x + this.xOffset && mouseX <= this.x + this.width && mouseY in this.y..this.y + this.height

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            if (!isExpanded) {
                if (isFolding && startAnimationTime != null) {
                    startAnimationTime = -startAnimationTime!! - animationTime + (currentTime) * 2 - expandDelay
                    isFolding = false
                } else if (startAnimationTime == null) {
                    startAnimationTime = currentTime
                }

                val elapsedTime = currentTime - startAnimationTime!!

                if (elapsedTime >= expandDelay) {
                    val progress =
                        ((elapsedTime - expandDelay).toFloat() / animationTime.toFloat()).coerceIn(0f, 1f)

                    xOffset = MathHelper.lerp(progress, maxXOffset, 0)

                    if (xOffset == 0) {
                        isExpanded = true
                        startAnimationTime = null
                        isExpanding = false
                    } else {
                        isExpanding = true
                    }
                }
            }
        } else if (xOffset < maxXOffset) {
            if (isExpanding && startAnimationTime != null) {
                startAnimationTime = -startAnimationTime!! - animationTime + (currentTime) * 2 - expandDelay
                isExpanding = false
            } else if (isExpanded && xOffset == 0 || startAnimationTime == null) {
                isExpanded = false
                startAnimationTime = currentTime
            }

            val elapsedTime = currentTime - startAnimationTime!!
            val progress = (elapsedTime.toFloat() / animationTime.toFloat()).coerceIn(0f, 1f)

            xOffset = MathHelper.lerp(progress, 0, maxXOffset)

            if (xOffset == maxXOffset) {
                startAnimationTime = null
                isFolding = false
            } else {
                isFolding = true
            }
        } else {
            startAnimationTime = null
        }

        context.drawTexture(
            this.texture,
            this.x + xOffset,
            this.y,
            textureU,
            textureV,
            getWidth() - xOffset,
            getHeight(),
            this.textureWidth,
            this.textureHeight
        )

        getText()?.let { text ->
            textRenderer?.let {
                context.drawText(
                    it,
                    text,
                    x + xOffset - 3 - it.getWidth(text),
                    y + (height - it.fontHeight) / 2 + 1,
                    11184810,
                    false
                )
            }
        }
    }

    fun getText(): Text? = textSupplier?.invoke()

    fun setTextSupplier(textRenderer: TextRenderer, supplier: (() -> Text)?): ExpandableIconButtonWidget {
        this.textRenderer = textRenderer
        textSupplier = supplier
        return this
    }
}