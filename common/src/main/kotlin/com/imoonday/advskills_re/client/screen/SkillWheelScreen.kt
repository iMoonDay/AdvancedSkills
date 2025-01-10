package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.render.*
import net.minecraft.text.*
import org.joml.*
import java.awt.*
import kotlin.math.*

class SkillWheelScreen : Screen(Text.empty()) {

    private var selectingSlot: Int? = null

    override fun renderBackground(context: DrawContext) = Unit

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        val player = clientPlayer ?: return
        var size = player.skillContainer.slotSize
        if (size < 0) size = 0

        val centerX = context.scaledWindowWidth / 2
        val centerY = context.scaledWindowHeight / 2
        val tip = translate("screen.wheel.tip", client!!.options.inventoryKey.boundKeyLocalizedText)
        var tipY = 6

        val textColor = 0xFFFFFF
        val backgroundColor = Color.GRAY.alpha(0.4).rgb

        textRenderer.wrapLines(tip, (context.scaledWindowWidth * 0.9).toInt()).forEach {
            context.drawTextWithBackground(textRenderer, it, centerX, tipY, textColor, backgroundColor)
            tipY += textRenderer.fontHeight + 2
        }
        if (size > 0) {
            val ringStyle = ClientConfig.get().useRingCastingWheel
            if (ringStyle) {
                context.matrices.push()
                drawCircle(centerX, centerY, 24f, 64f, size)
                context.matrices.pop()
            }
            val positions = calculatePositions(size)
            selectingSlot = findSlot(size, mouseX, mouseY, centerX, centerY)
            for (i in 0 until size) {
                val (x, y) = positions[i]
                val startX = centerX + x - 8
                val startY = centerY + y - 8
                if (!ringStyle) {
                    SkillSlotRenderer.renderSlot(context, startX - 2, startY - 2, i + 1 == selectingSlot)
                }
                SkillRenderer.renderIcon(player.getSkill(i + 1), context, startX, startY, null)
            }
        }

        context.drawTextWithBackground(
            textRenderer,
            selectingSlot?.let { player.getSkill(it).name }
                ?: if (size == 0) translate("screen.wheel.empty") else translate("screen.wheel.cancel"), centerX,
            centerY - 16 - 4, textColor, backgroundColor
        )
        selectingSlot?.let { slot ->
            ModKeyBindings.skillKeys.getOrNull(slot - 1)?.run {
                if (!this.isUnbound) {
                    context.drawTextWithBackground(
                        textRenderer,
                        boundKeyLocalizedText,
                        centerX,
                        centerY + 8 + 4,
                        textColor,
                        backgroundColor
                    )
                }
            }
            player.getSkill(slot)
                .takeIf { !it.disabled }
                ?.run {
                    var y = centerY + 64 + 3
                    textRenderer.textHandler
                        .wrapLines(description, (context.scaledWindowWidth * 0.65).toInt(), Style.EMPTY)
                        .forEach {
                            context.drawTextWithBackground(
                                textRenderer, it.string, centerX, y, textColor, backgroundColor
                            )
                            y += textRenderer.fontHeight + 2
                        }
                }
        }
    }

    private fun calculatePositions(n: Int): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        val radius = (24 + 64) / 2.0
        val angle = 2 * PI / n
        val offset = PI / 2
        for (i in 0 until n) {
            val theta = i * angle - offset
            val x = radius * cos(theta)
            val y = radius * sin(theta)
            result.add(Pair(x.toInt(), y.toInt()))
        }
        return result
    }

    private fun findSlot(n: Int, mouseX: Int, mouseY: Int, centerX: Int, centerY: Int): Int? {
        if (Vector2i.distance(mouseX, mouseY, centerX, centerY) < 24) return null
        val x = mouseX - centerX
        val y = mouseY - centerY
        var angle = atan2(y.toDouble(), x.toDouble())
        if (angle < 0) angle += 2 * PI
        val offset = PI / 2 + PI / n
        angle = (angle + 2 * PI + offset) % (2 * PI)
        val anglePerElement = 2 * PI / n
        return (angle / anglePerElement).toInt() + 1
    }

    override fun tick() {
        super.tick()
        handleMovement()
        if (!ModKeyBindings.QUICK_CAST.isPressedInScreen) close()
    }

    override fun close() {
        if (selectingSlot != null) {
            quickCastSlot = selectingSlot!!
        }
        super.close()
    }

    private fun requestUseSkill(index: Int) {
        val state = if (clientPlayer?.getSkill(index)
                ?.let { clientPlayer!!.isCharging(it) } == true
        ) UseSkillC2SRequest.KeyState.RELEASE else UseSkillC2SRequest.KeyState.PRESS
        clientPlayer?.requestUse(index, state)
    }

    override fun shouldPause(): Boolean = false

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return when (button) {
            0 -> {
                selectingSlot?.let(::requestUseSkill)
                close()
                true
            }

            1 -> {
                selectingSlot = null
                close()
                true
            }

            2 -> {
                selectingSlot?.let { index ->
                    clientPlayer?.getSkill(index)?.takeUnless { it.disabled }?.let {
                        client!!.setScreen(SkillGalleryScreen(it))
                    }
                } ?: run {
                    quickCastSlot = null
                    close()
                }
                true
            }

            else -> super.mouseClicked(mouseX, mouseY, button)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (client!!.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            client!!.setScreen(SkillInventoryScreen(clientPlayer!!))
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun handleMovement() {
        val options = client!!.options
        arrayOf(
            options.forwardKey,
            options.backKey,
            options.leftKey,
            options.rightKey,
            options.jumpKey,
            options.sprintKey,
            options.sneakKey
        ).forEach { it.isPressed = it.isPressedInScreen }
    }

    private fun drawCircle(
        centerX: Int,
        centerY: Int,
        radiusIn: Float,
        radiusOut: Float,
        segmentation: Int
    ) {
        val buffer = client!!.bufferBuilders.entityVertexConsumers.getBuffer(RenderLayer.getGui())
        val color = 0x3F000000
        val selectedColor = 0x3FFFFFFF

        repeat(segmentation) {
            val startAngle = (2 * PI * ((it - 0.5f) / segmentation - 0.25f)).toFloat()
            val endAngle = (2 * PI * ((it + 0.5f) / segmentation - 0.25f)).toFloat()
            drawPieArc(
                buffer, centerX.toDouble(), centerY.toDouble(), 0.0,
                radiusIn, radiusOut, startAngle, endAngle,
                if (it + 1 == selectingSlot) selectedColor else color
            )
        }
    }

    private fun drawPieArc(
        buffer: VertexConsumer,
        x: Double,
        y: Double,
        z: Double,
        radiusIn: Float,
        radiusOut: Float,
        startAngle: Float,
        endAngle: Float,
        color: Int
    ) {
        val angle = endAngle - startAngle
        val sections = max(1f, ceil(angle / PRECISION)).toInt()

        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = (color shr 0) and 0xFF
        val a = (color shr 24) and 0xFF

        val slice = angle / sections

        for (i in 0 until sections) {
            val angle1 = startAngle + i * slice
            val angle2 = startAngle + (i + 1) * slice

            val pos1InX = x + radiusIn * cos(angle1)
            val pos1InY = y + radiusIn * sin(angle1)
            val pos1OutX = x + radiusOut * cos(angle1)
            val pos1OutY = y + radiusOut * sin(angle1)
            val pos2OutX = x + radiusOut * cos(angle2)
            val pos2OutY = y + radiusOut * sin(angle2)
            val pos2InX = x + radiusIn * cos(angle2)
            val pos2InY = y + radiusIn * sin(angle2)

            buffer.vertex(pos1OutX, pos1OutY, z).color(r, g, b, a).next()
            buffer.vertex(pos1InX, pos1InY, z).color(r, g, b, a).next()
            buffer.vertex(pos2InX, pos2InY, z).color(r, g, b, a).next()
            buffer.vertex(pos2OutX, pos2OutY, z).color(r, g, b, a).next()
        }
    }

    companion object {

        private const val PRECISION: Float = 2.5f / 360.0f
        var quickCastSlot: Int? = null
    }
}