package com.imoonday.render

import com.imoonday.config.UIConfigModel
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Util
import java.awt.Color

class ScrollingStringRenderer(
    val text: Text,
    val maxWidth: Int,
    private val textRenderer: TextRenderer,
    var color: Int = text.style.color?.rgb ?: Color.WHITE.rgb,
    var shadow: Boolean = true,
) {

    constructor(
        text: String,
        maxWidth: Int,
        textRenderer: TextRenderer,
        color: Int = Color.WHITE.rgb,
        shadow: Boolean = true,
    ) : this(Text.literal(text), maxWidth, textRenderer, color, shadow)

    private val updateInterval: Double
        get() {
            val rate = UIConfigModel.instance.nameScrollRate
            if (rate <= 0.0) return Double.MAX_VALUE
            return 100 / rate
        }
    private var xOffset: Int = 0
    private var right = true
    private var lastUpdateTime: Long = Util.getMeasuringTimeMs()
    private var waitTime: Int = 0
    val width = textRenderer.getWidth(text)
    val fixed = width <= maxWidth

    fun update() {
        val currentTime = Util.getMeasuringTimeMs()
        if (waitTime > 0) {
            if (currentTime - lastUpdateTime < waitTime) {
                return
            } else {
                waitTime = 0
            }
        }
        if (currentTime - lastUpdateTime < updateInterval) return
        lastUpdateTime = currentTime
        if (right) {
            if (xOffset + maxWidth >= width) {
                right = false
                waitTime = 1000
            } else {
                xOffset++
            }
        } else {
            if (xOffset <= 0) {
                right = true
                waitTime = 1000
            } else {
                xOffset--
            }
        }
    }

    fun render(context: DrawContext, x: Int, y: Int) {
        context.enableScissor(x, y, x + maxWidth, y + textRenderer.fontHeight * 2)
        if (fixed) {
            context.drawText(textRenderer, text, x + maxWidth - width, y + textRenderer.fontHeight / 2, color, shadow)
        } else {
            context.drawText(textRenderer, text, x - xOffset, y + textRenderer.fontHeight / 2, color, shadow)
            update()
        }
        context.disableScissor()
    }
}