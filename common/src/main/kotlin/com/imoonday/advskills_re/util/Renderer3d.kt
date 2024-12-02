package com.imoonday.advskills_re.util

import com.mojang.blaze3d.systems.*
import net.minecraft.client.*
import net.minecraft.client.gl.*
import net.minecraft.client.render.*
import net.minecraft.client.texture.*
import net.minecraft.client.util.math.*
import net.minecraft.util.*
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
        runner: Consumer<BufferBuilder>,
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

    private fun useBufferWithTexture(
        texture: Identifier?,
        mode: VertexFormat.DrawMode,
        format: VertexFormat,
        shader: Supplier<ShaderProgram?>,
        runner: Consumer<BufferBuilder>,
    ) {
        val t = Tessellator.getInstance()
        val bb = t.buffer

        bb.begin(mode, format)

        runner.accept(bb)

        setupRender()
        RenderSystem.disableCull()
        texture?.let { RenderSystem.setShaderTexture(0, it) }
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
    fun renderEdged(stack: MatrixStack, colorFill: Color, colorOutline: Color, start: Vec3d, dimensions: Vec3d) {
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
        useBuffer(
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram
        ) {
            drawCube(it, matrix, x1, y1, z1, x2, y2, z2, fill)
        }

        useBuffer(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram
        ) {
            drawCubeOutline(it, matrix, x1, y1, z1, x2, y2, z2, outline)
        }
    }

    private fun drawCubeOutline(
        builder: BufferBuilder,
        matrix: Matrix4f,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        color: FloatArray
    ) {
        val (red, green, blue, alpha) = color

        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
    }

    private fun drawCubeOutline(
        builder: BufferBuilder,
        matrix: Matrix4f,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        color: FloatArray,
        facesToRender: Set<Direction> // 需要渲染的面
    ) {
        val (red, green, blue, alpha) = color

        //X轴朝东(右)，Y轴朝上，Z轴朝南(后)
        facesToRender.forEach { direction ->
            when (direction) {
                Direction.UP -> {
                    // 渲染上面边缘
                    if (Direction.WEST in facesToRender) {
                        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                    }

                    if (Direction.EAST in facesToRender) {
                        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    }

                    if (Direction.NORTH in facesToRender) {
                        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                    }

                    if (Direction.SOUTH in facesToRender) {
                        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    }
                }

                Direction.DOWN -> {
                    // 渲染下面边缘
                    if (Direction.WEST in facesToRender) {
                        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                    }

                    if (Direction.EAST in facesToRender) {
                        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    }

                    if (Direction.NORTH in facesToRender) {
                        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                    }

                    if (Direction.SOUTH in facesToRender) {
                        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    }
                }

                Direction.NORTH -> {
                    // 渲染北面边缘
                    if (Direction.UP in facesToRender) {
                        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                    }
                    if (Direction.DOWN in facesToRender) {
                        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                    }
                    if (Direction.WEST in facesToRender) {
                        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                    }
                    if (Direction.EAST in facesToRender) {
                        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                    }
                }

                Direction.SOUTH -> {
                    // 渲染南面边缘
                    if (Direction.UP in facesToRender) {
                        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.DOWN in facesToRender) {
                        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.WEST in facesToRender) {
                        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.EAST in facesToRender) {
                        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    }
                }

                Direction.WEST -> {
                    // 渲染西面边缘
                    if (Direction.UP in facesToRender) {
                        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.DOWN in facesToRender) {
                        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.NORTH in facesToRender) {
                        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                    }
                    if (Direction.SOUTH in facesToRender) {
                        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                    }
                }

                Direction.EAST -> {
                    // 渲染东面边缘
                    if (Direction.UP in facesToRender) {
                        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.DOWN in facesToRender) {
                        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    }
                    if (Direction.NORTH in facesToRender) {
                        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                    }
                    if (Direction.SOUTH in facesToRender) {
                        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    }
                }
            }
        }
    }

    private fun drawCube(
        builder: BufferBuilder,
        matrix: Matrix4f,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        color: FloatArray
    ) {
        val (red, green, blue, alpha) = color

        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()

        builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
        builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
    }

    private fun drawCube(
        builder: BufferBuilder,
        matrix: Matrix4f,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        color: FloatArray,
        facesToRender: Set<Direction> // 指定需要渲染的面
    ) {
        val (red, green, blue, alpha) = color

        facesToRender.forEach { direction ->
            when (direction) {
                Direction.UP -> {
                    // 上面
                    builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                }

                Direction.DOWN -> {
                    // 下面
                    builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                }

                Direction.NORTH -> {
                    // 前面
                    builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                }

                Direction.SOUTH -> {
                    // 后面
                    builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                }

                Direction.WEST -> {
                    // 左面
                    builder.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next()
                }

                Direction.EAST -> {
                    // 右面
                    builder.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next()
                    builder.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next()
                }
            }
        }
    }

    fun renderVisibleFaces(
        stack: MatrixStack,
        blocks: List<BlockRenderInfo>,
        fill: Boolean = true
    ) {
        val matrix = stack.peek().positionMatrix

        val blockWithFaces = mutableMapOf<BlockPos, MutableSet<Direction>>()
        val map = blocks.associate { it.pos to it.colorOutline }
        blocks.forEach { block ->
            Direction.entries.forEach { direction ->
                val pos = block.pos
                val color = map[pos.offset(direction)]
                if (color == null || block.colorOutline.run { red != color.red || green != color.green || blue != color.blue }) {
                    blockWithFaces.getOrPut(pos) { mutableSetOf() }.add(direction)
                }
            }
        }

        if (fill) {
            RenderSystem.disableCull()
            useBuffer(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR,
                GameRenderer::getPositionColorProgram
            ) { buffer ->
                blocks.forEach { block ->
                    val faces = blockWithFaces[block.pos] ?: return@forEach
                    val start = transformVec3d(block.start)
                    val end = transformVec3d(block.end)
                    drawCube(
                        buffer,
                        matrix,
                        start.x.toFloat(),
                        start.y.toFloat(),
                        start.z.toFloat(),
                        end.x.toFloat(),
                        end.y.toFloat(),
                        end.z.toFloat(),
                        getColor(block.colorFill),
                        faces
                    )
                }
            }
        }

        useBuffer(
            VertexFormat.DrawMode.DEBUG_LINES,
            VertexFormats.POSITION_COLOR,
            GameRenderer::getPositionColorProgram
        ) { buffer ->
            blocks.forEach { block ->
                val faces = blockWithFaces[block.pos] ?: return@forEach
                val start = transformVec3d(block.start)
                val end = transformVec3d(block.end)
                drawCubeOutline(
                    buffer,
                    matrix,
                    start.x.toFloat(),
                    start.y.toFloat(),
                    start.z.toFloat(),
                    end.x.toFloat(),
                    end.y.toFloat(),
                    end.z.toFloat(),
                    getColor(block.colorOutline),
                    faces
                )
            }
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
        action: RenderAction,
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
        p4: Vec3d,
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
        alphaOverwrite: Int,
    ): Color = Color(
        if (redOverwrite == -1) original.red else redOverwrite,
        if (greenOverwrite == -1) original.green else greenOverwrite,
        if (blueOverwrite == -1) original.blue else blueOverwrite,
        if (alphaOverwrite == -1) original.alpha else alphaOverwrite
    )

    fun renderSprite(
        stack: MatrixStack,
        sprite: Sprite,
        leftTop: Vec3d,
        leftBottom: Vec3d,
        rightBottom: Vec3d,
        rightTop: Vec3d
    ) = renderTexturedQuad(
        stack,
        sprite.atlasId,
        leftTop,
        leftBottom,
        rightBottom,
        rightTop,
        sprite.minU,
        sprite.minV,
        sprite.maxU,
        sprite.maxV
    )

    fun renderTexturedQuad(
        stack: MatrixStack,
        texture: Identifier,
        leftTop: Vec3d, leftBottom: Vec3d, rightBottom: Vec3d, rightTop: Vec3d,
        u1: Float, v1: Float, u2: Float, v2: Float,
    ) {
        val matrix = stack.peek().positionMatrix

        useBufferWithTexture(
            texture,
            VertexFormat.DrawMode.QUADS,
            VertexFormats.POSITION_TEXTURE,
            GameRenderer::getPositionTexProgram
        ) { buffer ->
            buffer.vertex(matrix, leftTop.x.toFloat(), leftTop.y.toFloat(), leftTop.z.toFloat())
                .texture(u1, v1)
                .next()

            buffer.vertex(matrix, leftBottom.x.toFloat(), leftBottom.y.toFloat(), leftBottom.z.toFloat())
                .texture(u1, v2)
                .next()

            buffer.vertex(matrix, rightBottom.x.toFloat(), rightBottom.y.toFloat(), rightBottom.z.toFloat())
                .texture(u2, v2)
                .next()

            buffer.vertex(matrix, rightTop.x.toFloat(), rightTop.y.toFloat(), rightTop.z.toFloat())
                .texture(u2, v1)
                .next()
        }
    }

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
            matrix: Matrix4f?,
        )
    }

    data class FadingBlock(
        val outline: Color,
        val fill: Color,
        val start: Vec3d,
        val dimensions: Vec3d,
        val created: Long,
        val lifeTime: Long,
    ) {

        val lifeTimeLeft: Long
            get() = max(0.0, (created - System.currentTimeMillis() + lifeTime).toDouble()).toLong()
        val isDead: Boolean
            get() = lifeTimeLeft == 0L
    }

    data class BlockRenderInfo(
        val pos: BlockPos,
        val start: Vec3d,
        val end: Vec3d,
        val colorFill: Color,
        val colorOutline: Color
    )
}