package com.imoonday.util

import com.mojang.blaze3d.systems.*
import net.minecraft.client.*
import net.minecraft.client.gl.*
import net.minecraft.client.render.*
import net.minecraft.client.util.math.*
import net.minecraft.util.math.*
import org.jetbrains.annotations.*
import org.joml.*
import org.lwjgl.opengl.*
import java.awt.*
import java.util.concurrent.*
import java.util.function.*
import kotlin.math.*

/**
 * From https://github.com/0x3C50/Renderer
 */
object Renderer3d {

    val fades: MutableList<FadingBlock> = CopyOnWriteArrayList()
    private val client: MinecraftClient = MinecraftClient.getInstance()
    private var renderThroughWalls = false

    /**
     * Starts rendering through walls
     */
    fun renderThroughWalls() {
        renderThroughWalls = true
    }

    /**
     * Stops rendering through walls
     */
    fun stopRenderThroughWalls() {
        renderThroughWalls = false
    }

    /**
     * Returns true if the renderer is currently configured to render through walls
     *
     * @return True if the renderer is currently configured to render through walls
     */
    fun rendersThroughWalls(): Boolean = renderThroughWalls

    private fun setupRender() {
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(if (renderThroughWalls) GL11.GL_ALWAYS else GL11.GL_LEQUAL)
    }

    private fun endRender() {
        RenderSystem.enableCull()
        RenderSystem.disableBlend()
    }

    /**
     * Renders a fading block, that gets more transparent with time
     *
     * @param outlineColor The color of the outline
     * @param fillColor    The color of the filling
     * @param start        Start coordinate of the block
     * @param dimensions   Dimensions of the block
     * @param lifeTimeMs   The lifetime of the block, in millis
     */
    fun renderFadingBlock(outlineColor: Color, fillColor: Color, start: Vec3d, dimensions: Vec3d, lifeTimeMs: Long) {
        val fb = FadingBlock(
            outlineColor, fillColor, start, dimensions, System.currentTimeMillis(),
            lifeTimeMs
        )

        fades.removeIf { it.start == start && it.dimensions == dimensions }
        fades.add(fb)
    }

    /**
     * Renders all fading blocks. **For internal use only. You should have a good reason to call this yourself (don't).**
     *
     * @param stack The MatrixStack
     */
    @ApiStatus.Internal
    fun renderFadingBlocks(stack: MatrixStack) {
        fades.removeIf(FadingBlock::isDead)
        for (fade in fades) {
            val lifetimeLeft = fade.lifeTimeLeft
            var progress = lifetimeLeft / fade.lifeTime.toDouble()
            progress = MathHelper.clamp(progress, 0.0, 1.0)
            val ip = 1 - progress
            val out = modifyColor(fade.outline, -1, -1, -1, (fade.outline.alpha * progress).toInt())
            val fill = modifyColor(fade.fill, -1, -1, -1, (fade.fill.alpha * progress).toInt())
            renderEdged(
                stack, fill, out, fade.start.add(Vec3d(0.2, 0.2, 0.2).multiply(ip)),
                fade.dimensions.subtract(Vec3d(.4, .4, .4).multiply(ip))
            )
        }
    }

    private fun transformVec3d(vec3d: Vec3d): Vec3d {
        val camera = client.gameRenderer.camera
        val camPos = camera.pos
        return vec3d.subtract(camPos)
    }

    fun getColor(c: Color): FloatArray = floatArrayOf(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f)

    private fun useBuffer(
        mode: VertexFormat.DrawMode,
        format: VertexFormat,
        shader: Supplier<ShaderProgram?>,
        runner: Consumer<BufferBuilder>
    ) {
        val t = Tessellator.getInstance()
        val bb = t.buffer

        bb.begin(mode, format)

        runner.accept(bb)

        setupRender()
        RenderSystem.setShader(shader)
        BufferRenderer.drawWithGlobalProgram(bb.end())
        endRender()
    }

    /**
     * Renders a block outline
     *
     * @param stack      The MatrixStack
     * @param color      The color of the outline
     * @param start      Start position of the block
     * @param dimensions Dimensions of the block
     */
    fun renderOutline(stack: MatrixStack, color: Color, start: Vec3d, dimensions: Vec3d) {
        val m = stack.peek().positionMatrix
        genericAABBRender(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram,
            m,
            start,
            dimensions,
            color
        ) { buffer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, matrix ->
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        }
    }

    /**
     * Renders both a filled and outlined block
     *
     * @param stack        The MatrixStack
     * @param colorFill    The color of the filling
     * @param colorOutline The color of the outline
     * @param start        The start coordinate
     * @param dimensions   The dimensions
     */
    fun renderEdged(stack: MatrixStack, colorFill: Color, colorOutline: Color, start: Vec3d, dimensions: Vec3d?) {
        val matrix = stack.peek().positionMatrix
        val fill = getColor(colorFill)
        val outline = getColor(colorOutline)
        val vec3d = transformVec3d(start)
        val end = vec3d.add(dimensions)
        val x1 = vec3d.x.toFloat()
        val y1 = vec3d.y.toFloat()
        val z1 = vec3d.z.toFloat()
        val x2 = end.x.toFloat()
        val y2 = end.y.toFloat()
        val z2 = end.z.toFloat()
        val redFill = fill[0]
        val greenFill = fill[1]
        val blueFill = fill[2]
        val alphaFill = fill[3]
        val redOutline = outline[0]
        val greenOutline = outline[1]
        val blueOutline = outline[2]
        val alphaOutline = outline[3]
        useBuffer(
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram
        ) {
            it.vertex(matrix, x1, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()

            it.vertex(matrix, x1, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()

            it.vertex(matrix, x2, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()

            it.vertex(matrix, x2, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()

            it.vertex(matrix, x1, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()

            it.vertex(matrix, x1, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x2, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
            it.vertex(matrix, x1, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
        }

        useBuffer(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram
        ) {
            it.vertex(matrix, x1, y1, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y1, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y1, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y1, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y1, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y1, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y1, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y1, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()

            it.vertex(matrix, x1, y2, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y2, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y2, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y2, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y2, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y2, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y2, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y2, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()

            it.vertex(matrix, x1, y1, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y2, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()

            it.vertex(matrix, x2, y1, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y2, z1).color(redOutline, greenOutline, blueOutline, alphaOutline).next()

            it.vertex(matrix, x2, y1, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x2, y2, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()

            it.vertex(matrix, x1, y1, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
            it.vertex(matrix, x1, y2, z2).color(redOutline, greenOutline, blueOutline, alphaOutline).next()
        }
    }

    private fun genericAABBRender(
        mode: VertexFormat.DrawMode,
        format: VertexFormat,
        shader: Supplier<ShaderProgram?>,
        stack: Matrix4f,
        start: Vec3d,
        dimensions: Vec3d,
        color: Color,
        action: RenderAction
    ) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f
        val vec3d = transformVec3d(start)
        val end = vec3d.add(dimensions)
        val x1 = vec3d.x.toFloat()
        val y1 = vec3d.y.toFloat()
        val z1 = vec3d.z.toFloat()
        val x2 = end.x.toFloat()
        val y2 = end.y.toFloat()
        val z2 = end.z.toFloat()
        useBuffer(mode, format, shader) {
            action.run(it, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, stack)
        }
    }

    /**
     * Renders a filled block
     *
     * @param stack      The MatrixStack
     * @param color      The color of the filling
     * @param start      Start coordinates
     * @param dimensions Dimensions
     */
    fun renderFilled(stack: MatrixStack, color: Color, start: Vec3d, dimensions: Vec3d) {
        val s = stack.peek().positionMatrix
        genericAABBRender(
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram,
            s,
            start,
            dimensions,
            color
        ) { buffer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, matrix ->
            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()

            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        }
    }

    /**
     * Renders a simple line from `start` to `end`
     *
     * @param matrices The MatrixStack
     * @param color    The color of the line
     * @param start    The start coordinate
     * @param end      The end coordinate
     */
    fun renderLine(matrices: MatrixStack, color: Color, start: Vec3d, end: Vec3d) {
        val s = matrices.peek().positionMatrix
        genericAABBRender(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram,
            s,
            start,
            end.subtract(start),
            color
        ) { buffer, x, y, z, x1, y1, z1, red, green, blue, alpha, matrix ->
            buffer.vertex(matrix, x, y, z).color(red, green, blue, alpha).next()
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        }
    }

    fun renderQuad(
        stack: MatrixStack,
        color: Color,
        p1: Vec3d,
        p2: Vec3d,
        p3: Vec3d,
        p4: Vec3d
    ) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        val matrix = stack.peek().positionMatrix

        useBuffer(
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram
        ) { buffer ->
            // 将四个点分别传入顶点缓冲区
            buffer.vertex(matrix, p1.x.toFloat(), p1.y.toFloat(), p1.z.toFloat())
                .color(red, green, blue, alpha)
                .next()

            buffer.vertex(matrix, p2.x.toFloat(), p2.y.toFloat(), p2.z.toFloat())
                .color(red, green, blue, alpha)
                .next()

            buffer.vertex(matrix, p3.x.toFloat(), p3.y.toFloat(), p3.z.toFloat())
                .color(red, green, blue, alpha)
                .next()

            buffer.vertex(matrix, p4.x.toFloat(), p4.y.toFloat(), p4.z.toFloat())
                .color(red, green, blue, alpha)
                .next()
        }
    }

    /**
     * @param original       the original color
     * @param redOverwrite   the new red (or -1 for original)
     * @param greenOverwrite the new green (or -1 for original)
     * @param blueOverwrite  the new blue (or -1 for original)
     * @param alphaOverwrite the new alpha (or -1 for original)
     * @return the modified color
     */
    fun modifyColor(
        original: Color,
        redOverwrite: Int,
        greenOverwrite: Int,
        blueOverwrite: Int,
        alphaOverwrite: Int
    ): Color = Color(
        if (redOverwrite == -1) original.red else redOverwrite,
        if (greenOverwrite == -1) original.green else greenOverwrite,
        if (blueOverwrite == -1) original.blue else blueOverwrite,
        if (alphaOverwrite == -1) original.alpha else alphaOverwrite
    )

    internal fun interface RenderAction {

        fun run(
            buffer: BufferBuilder,
            x: Float,
            y: Float,
            z: Float,
            x1: Float,
            y1: Float,
            z1: Float,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float,
            matrix: Matrix4f?
        )
    }

    data class FadingBlock(
        val outline: Color,
        val fill: Color,
        val start: Vec3d,
        val dimensions: Vec3d,
        val created: Long,
        val lifeTime: Long
    ) {

        val lifeTimeLeft: Long
            get() = max(0.0, (created - System.currentTimeMillis() + lifeTime).toDouble()).toLong()
        val isDead: Boolean
            get() = lifeTimeLeft == 0L
    }
}