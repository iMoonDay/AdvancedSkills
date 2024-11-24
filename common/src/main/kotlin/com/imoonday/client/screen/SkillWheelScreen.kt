package com.imoonday.client.screen

import com.imoonday.client.*
import com.imoonday.network.c2s.*
import com.imoonday.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
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
        if (size <= 0) size = 1
        val centerX = context.scaledWindowWidth / 2
        val centerY = context.scaledWindowHeight / 2
        val tip = translate("screen", "wheel.tip", client!!.options.inventoryKey.boundKeyLocalizedText)
        var tipY = 6
        textRenderer.wrapLines(tip, (context.scaledWindowWidth * 0.9).toInt()).forEach {
            context.drawTextWithBackground(
                it,
                centerX,
                tipY,
                Color.WHITE.rgb,
                Color.GRAY.alpha(0.4).rgb
            )
            tipY += textRenderer.fontHeight + 2
        }
        val positions = calculatePositions(size)
        selectingSlot = findSlot(size, mouseX, mouseY, centerX, centerY)
        for (i in 0 until size) {
            val (x, y) = positions[i]
            val startX = centerX + x - 8
            val startY = centerY + y - 8
            player.getSkill(i + 1).renderIcon(context, startX, startY, player)
            if (selectingSlot == i + 1) {
                context.drawBorder(
                    startX - 1,
                    startY - 1,
                    16 + 2,
                    16 + 2,
                    Color.GREEN.rgb
                )
            }
        }
        context.drawTextWithBackground(
            selectingSlot?.let { player.getSkill(it).name } ?: translate("screen", "wheel.cancel"),
            centerX,
            centerY - 16 - 4,
            Color.WHITE.rgb,
            Color.GRAY.alpha(0.4).rgb
        )
        selectingSlot?.let {
            ModKeyBindings.skillKeys.getOrNull(it - 1)?.run {
                if (!this.isUnbound) context.drawTextWithBackground(
                    boundKeyLocalizedText,
                    centerX,
                    centerY + 8 + 4,
                    Color.WHITE.rgb,
                    Color.GRAY.alpha(0.4).rgb
                )
            }
            it.let { player.getSkill(it) }.takeIf { !it.invalid }?.run {
                var y = centerY + 60
                textRenderer.textHandler.wrapLines(description, (context.scaledWindowWidth * 0.65).toInt(), Style.EMPTY)
                    .forEach {
                        context.drawTextWithBackground(
                            it.string,
                            centerX,
                            y,
                            Color.WHITE.rgb,
                            Color.GRAY.alpha(0.4).rgb
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
        if (Vector2i.distance(mouseX, mouseY, centerX, centerY) < 5) return null
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
                    clientPlayer?.getSkill(index)?.takeUnless { it.invalid }?.let {
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
        ).forEach {
            it.isPressed = it.isPressedInScreen
        }
    }

    companion object {

        var quickCastSlot: Int? = null
    }
}