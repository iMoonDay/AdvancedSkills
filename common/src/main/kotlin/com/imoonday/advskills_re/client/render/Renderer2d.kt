package com.imoonday.advskills_re.client.render

import com.imoonday.advskills_re.client.render.Renderer2d.SLIDER_HEIGHT
import com.imoonday.advskills_re.client.render.Renderer2d.SLIDER_WIDTH
import com.imoonday.advskills_re.client.render.Renderer2d.sliderTexture
import com.imoonday.advskills_re.client.render.Renderer2d.white20
import com.imoonday.advskills_re.client.render.Renderer2d.white40
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.font.*
import net.minecraft.client.gui.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.text.*
import net.minecraft.util.math.*
import org.joml.*
import java.awt.*
import java.lang.Math
import kotlin.math.*

object Renderer2d {

    const val SLIDER_WIDTH = 4
    const val SLIDER_HEIGHT = 9
    val sliderTexture = id("textures/gui/slider.png")
    val white20 = Color.WHITE.alpha(0.2).rgb
    val white40 = Color.WHITE.alpha(0.4).rgb

    fun renderTexture(
        matrices: MatrixStack,
        x0: Double,
        y0: Double,
        width: Double,
        height: Double,
        u: Float,
        v: Float,
        regionWidth: Double,
        regionHeight: Double,
        textureWidth: Double,
        textureHeight: Double,
    ) {
        val x1 = x0 + width
        val y1 = y0 + height
        val z = 0.0
        renderTexturedQuad(
            matrices.peek().positionMatrix,
            x0,
            x1,
            y0,
            y1,
            z,
            (u + 0.0f) / textureWidth.toFloat(),
            (u + regionWidth.toFloat()) / textureWidth.toFloat(),
            (v + 0.0f) / textureHeight.toFloat(),
            (v + regionHeight.toFloat()) / textureHeight.toFloat()
        )
    }

    private fun renderTexturedQuad(
        matrix: Matrix4f,
        x0: Double,
        x1: Double,
        y0: Double,
        y1: Double,
        z: Double,
        u0: Float,
        u1: Float,
        v0: Float,
        v1: Float,
    ) {
        val buffer = Tessellator.getInstance().buffer
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        buffer.vertex(matrix, x0.toFloat(), y1.toFloat(), z.toFloat()).texture(u0, v1).next()
        buffer.vertex(matrix, x1.toFloat(), y1.toFloat(), z.toFloat()).texture(u1, v1).next()
        buffer.vertex(matrix, x1.toFloat(), y0.toFloat(), z.toFloat()).texture(u1, v0).next()
        buffer.vertex(matrix, x0.toFloat(), y0.toFloat(), z.toFloat()).texture(u0, v0).next()

        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        draw(buffer)
    }

    fun draw(builder: BufferBuilder) {
        BufferRenderer.drawWithGlobalProgram(builder.end())
    }

    fun renderTooltip(context: DrawContext, x: Int, y: Int, width: Int, height: Int, z: Int) {
        val i = x - 3
        val j = y - 3
        val k = width + 3 + 3
        val l = height + 3 + 3
        renderHorizontalLine(context, i, j - 1, k, z, -267386864)
        renderHorizontalLine(context, i, j + l, k, z, -267386864)
        renderRectangle(context, i, j, k, l, z, -267386864)
        renderVerticalLine(context, i - 1, j, l, z, -267386864)
        renderVerticalLine(context, i + k, j, l, z, -267386864)
        renderBorder(context, i, j + 1, k, l, z, 1347420415, 1344798847)
    }

    private fun renderBorder(
        context: DrawContext,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        z: Int,
        startColor: Int,
        endColor: Int,
    ) {
        renderVerticalLine(context, x, y, height - 2, z, startColor, endColor)
        renderVerticalLine(context, x + width - 1, y, height - 2, z, startColor, endColor)
        renderHorizontalLine(context, x, y - 1, width, z, startColor)
        renderHorizontalLine(context, x, y - 1 + height - 1, width, z, endColor)
    }

    private fun renderVerticalLine(context: DrawContext, x: Int, y: Int, height: Int, z: Int, color: Int) {
        context.fill(x, y, x + 1, y + height, z, color)
    }

    private fun renderVerticalLine(
        context: DrawContext,
        x: Int,
        y: Int,
        height: Int,
        z: Int,
        startColor: Int,
        endColor: Int,
    ) {
        context.fillGradient(x, y, x + 1, y + height, z, startColor, endColor)
    }

    private fun renderHorizontalLine(context: DrawContext, x: Int, y: Int, width: Int, z: Int, color: Int) {
        context.fill(x, y, x + width, y + 1, z, color)
    }

    private fun renderRectangle(context: DrawContext, x: Int, y: Int, width: Int, height: Int, z: Int, color: Int) {
        context.fill(x, y, x + width, y + height, z, color)
    }
}

fun DrawContext.renderPanel(x: Int, y: Int, width: Int, height: Int) =
    renderPanel(x, y, width, height, 0xFFC6C6C6.toInt(), 0xFFFFFFFF.toInt(), 0xFF555555.toInt())

fun DrawContext.renderDarkPanel(x: Int, y: Int, width: Int, height: Int) {
    renderPanel(x, y, width, height, 0xFF2F2F2F.toInt(), 0xFF414141.toInt(), 0xFF0B0B0B.toInt())
}

fun DrawContext.renderPanel(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    centerColor: Int,
    lightEdgeColor: Int,
    darkEdgeColor: Int,
    borderColor: Int = 0xFF000000.toInt(),
) {
    if (width < 8 || height < 8) throw IllegalArgumentException("Panel size must be at least 8x8")
    val rightX = x + width
    val bottomY = y + height

    this.fill(x + 2, y + 2, rightX - 2, bottomY - 2, centerColor)

    this.fill(x + 1, y + 1, x + 3, bottomY - 3, lightEdgeColor)
    this.fill(x + 3, y + 1, rightX - 3, y + 3, lightEdgeColor)
    this.fill(x + 3, y + 3, x + 4, y + 4, lightEdgeColor)

    this.fill(rightX - 3, y + 3, rightX - 1, bottomY - 1, darkEdgeColor)
    this.fill(x + 3, bottomY - 3, rightX - 2, bottomY, darkEdgeColor)
    this.fill(rightX - 4, bottomY - 4, rightX - 3, bottomY - 3, darkEdgeColor)

    this.fill(x + 1, y + 1, x + 2, y + 2, borderColor)
    this.drawHorizontalLine(x + 2, rightX - 4, y, borderColor)
    this.fill(rightX - 3, y + 1, rightX - 2, y + 2, borderColor)
    this.fill(rightX - 2, y + 2, rightX - 1, y + 3, borderColor)
    this.drawVerticalLine(rightX - 1, y + 2, bottomY - 2, borderColor)
    this.fill(rightX - 2, bottomY - 2, rightX - 1, bottomY - 1, borderColor)
    this.drawHorizontalLine(x + 3, rightX - 3, bottomY - 1, borderColor)
    this.fill(x + 2, bottomY - 2, x + 3, bottomY - 1, borderColor)
    this.fill(x + 1, bottomY - 3, x + 2, bottomY - 2, borderColor)
    this.drawVerticalLine(x, y + 1, bottomY - 3, borderColor)
}

fun DrawContext.renderPanelInset(x: Int, y: Int, width: Int, height: Int) {
    if (width < 2 || height < 2) throw IllegalArgumentException("Panel size must be at least 2x2")
    val rightX = x + width
    val bottomY = y + height

    this.drawVerticalLine(x, y, bottomY, 0xFF000000.toInt())
    this.drawHorizontalLine(x, rightX - 2, y, 0xFF000000.toInt())
    this.fill(rightX - 1, y, rightX, y + 1, 0xFF555555.toInt())
    this.drawVerticalLine(rightX - 1, y, bottomY, 0xFFFFFFFF.toInt())
    this.drawHorizontalLine(x + 1, rightX - 2, bottomY - 1, 0xFFFFFFFF.toInt())
    this.fill(x, bottomY - 1, x + 1, bottomY, 0xFF555555.toInt())
}

fun DrawContext.renderTooltip(x: Int, y: Int, width: Int, height: Int) {
    val buffer = Tessellator.getInstance().buffer
    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

    Renderer2d.renderTooltip(this, x + 4, y + 4, width - 8, height - 8, 0)

    RenderSystem.enableBlend()
    RenderSystem.defaultBlendFunc()
    RenderSystem.setShader(GameRenderer::getPositionColorProgram)

    Tessellator.getInstance().draw()
}

fun DrawContext.renderVanillaTranslucent(x: Int, y: Int, width: Int, height: Int) {
    fillGradient(x, y, x + width, y + height, 0xC0101010.toInt(), 0xD0101010.toInt())
}

fun DrawContext.drawScrollableText(
    textRenderer: TextRenderer,
    text: Text,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    color: Int,
    shadow: Boolean = true,
    center: Boolean = true,
) {
    val textWidth = textRenderer.getWidth(text)
    val y = (top + bottom - 9) / 2 + 1
    val width = right - left
    if (textWidth > width) {
        val l = textWidth - width
        val d = System.currentTimeMillis().toDouble() / 1000.0
        val e = max(l.toDouble() * 0.5, 3.0)
        val f = sin((Math.PI / 2) * cos((Math.PI * 2) * d / e)) / 2.0 + 0.5
        val g = MathHelper.lerp(f, 0.0, l.toDouble())
        enableScissor(left, top, right, bottom)
        drawText(textRenderer, text, left - g.toInt(), y, color, shadow)
        disableScissor()
    } else {
        drawText(textRenderer, text, if (center) (left + right - textWidth) / 2 else left, y, color, shadow)
    }
}

fun DrawContext.overlayHighlightWithSize(x: Int, y: Int, width: Int, height: Int, selected: Boolean) =
    overlayHighlight(x, y, x + width, y + height, selected)

fun DrawContext.overlayHighlight(x1: Int, y1: Int, x2: Int, y2: Int, selected: Boolean) =
    fill(x1, y1, x2, y2, if (selected) white40 else white20)

fun DrawContext.renderScrollbar(x: Int, y: Int, height: Int, scrollAmount: Int, totalAmount: Int) {
    val sliderY =
        if (totalAmount == 0) null else (y + 1 + (height - SLIDER_HEIGHT) * (scrollAmount.toFloat() / totalAmount.toFloat())).toInt()
    renderScrollbar(x, y, height, sliderY)
}

fun DrawContext.renderScrollbar(x: Int, y: Int, height: Int, sliderY: Int?) {
    renderScrollbar(x, y, height)
    if (sliderY == null) {
        setShaderColor(0.8f, 0.8f, 0.8f, 1f)
    }
    drawTexture(
        sliderTexture,
        x + 1,
        sliderY?.coerceIn(y + 1, y + 1 + height - SLIDER_HEIGHT) ?: (y + 1),
        SLIDER_WIDTH,
        SLIDER_HEIGHT,
        0f,
        3f,
        SLIDER_WIDTH,
        SLIDER_HEIGHT,
        16,
        16
    )
    if (sliderY == null) {
        setShaderColor(1f, 1f, 1f, 1f)
    }
}

fun DrawContext.renderScrollbar(x: Int, y: Int, height: Int) {
    this.fill(x, y, x + 1, y + height, 0xFF373737.toInt())
    this.fill(x + 1, y, x + 5, y + 1, 0xFF373737.toInt())
    this.fill(x + 5, y, x + 6, y + 1, 0xFF8B8B8B.toInt())
    this.fill(x + 5, y + 1, x + 6, y + height, 0xFFFFFFFF.toInt())
    this.fill(x + 1, y + height - 1, x + 5, y + height, 0xFFFFFFFF.toInt())
    this.fill(x, y + height - 1, x + 1, y + height, 0xFF8B8B8B.toInt())
    this.fill(x + 1, y + 1, x + 5, y + height - 1, 0xFF8B8B8B.toInt())
}

fun DrawContext.drawTextWithBackground(
    textRenderer: TextRenderer,
    text: String,
    centerX: Int,
    y: Int,
    color: Int,
    backgroundColor: Int,
    shadow: Boolean = true,
) = drawTextWithBackground(textRenderer, text.toText(), centerX, y, color, backgroundColor, shadow)

fun DrawContext.drawTextWithBackground(
    textRenderer: TextRenderer,
    text: Text,
    centerX: Int,
    y: Int,
    color: Int,
    backgroundColor: Int,
    shadow: Boolean = true,
) = drawTextWithBackground(textRenderer, text.asOrderedText(), centerX, y, color, backgroundColor, shadow)

fun DrawContext.drawTextWithBackground(
    textRenderer: TextRenderer,
    text: OrderedText,
    centerX: Int,
    y: Int,
    color: Int,
    backgroundColor: Int,
    shadow: Boolean = true,
) {
    val width = textRenderer.getWidth(text)
    val x = centerX - width / 2
    fill(x - 1, y - 1, x + width + 1, y + textRenderer.fontHeight + 1, backgroundColor)
    drawText(textRenderer, text, x, y, color, shadow)
}

fun DrawContext.drawBox(x: Int, y: Int, width: Int, height: Int, focused: Boolean) {
    val i = if (focused) -1 else -6250336
    fill(x, y, x + width, y + height, i)
    fill(x + 1, y + 1, x + width - 1, y + height - 1, -16777216)
}

fun DrawContext.drawScaledText(
    textRenderer: TextRenderer,
    text: Text,
    maxWidth: Int,
    centerX: Int,
    y: Int,
    color: Int,
    shadow: Boolean = true
) {

    val textWidth = textRenderer.getWidth(text)
    if (textWidth > maxWidth) {
        val matrices = matrices
        val scale = maxWidth.toFloat() / textWidth

        matrices.push()
        matrices.translate(centerX.toFloat(), y.toFloat(), 0f)
        matrices.scale(scale, scale, 1f)
        drawText(textRenderer, text, -textWidth / 2, 0, color, shadow)
        matrices.pop()
    } else {
        drawText(textRenderer, text, centerX - textWidth / 2, y, color, shadow)
    }
}

fun DrawContext.drawScaledText(
    textRenderer: TextRenderer,
    text: String,
    maxWidth: Int,
    centerX: Int,
    y: Int,
    color: Int,
    shadow: Boolean = true
) = drawScaledText(textRenderer, text.toText(), maxWidth, centerX, y, color, shadow)