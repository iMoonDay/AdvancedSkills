package com.imoonday.screen

import com.imoonday.init.ModKeyBindings
import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.trigger.LongPressTrigger
import com.imoonday.util.*
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.joml.Vector2i
import java.awt.Color
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
        val positions = calculatePositions(size)
        selectingSlot = findSlot(size, mouseX, mouseY, centerX, centerY)
        for (i in 0 until size) {
            val (x, y) = positions[i]
            val startX = centerX + x - 8
            val startY = centerY + y - 8
            val skill = player.getSkill(i + 1)
            context.drawTexture(skill.icon, startX, startY, 0f, 0f, 16, 16, 16, 16)
            if (selectingSlot == i + 1) {
                context.drawBorder(
                    startX - 1,
                    startY - 1,
                    16 + 2,
                    16 + 2,
                    Color.GREEN.rgb
                )
                context.drawCenteredTextWithShadow(
                    client!!.textRenderer,
                    skill.name,
                    centerX,
                    centerY - 16,
                    Color.WHITE.rgb
                )
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
        if (!hasKeyDown(KeyBindingHelper.getBoundKeyOf(ModKeyBindings.QUICK_CAST).code)) close()
    }

    private fun hasKeyDown(keyCode: Int): Boolean = InputUtil.isKeyPressed(client!!.window.handle, keyCode)

    override fun close() {
        selectingSlot?.let { index ->
            val state = if (clientPlayer?.getSkill(index)
                    ?.let { it is LongPressTrigger && clientPlayer!!.isUsing(it) } == true
            ) UseSkillC2SRequest.KeyState.RELEASE else UseSkillC2SRequest.KeyState.PRESS
            clientPlayer?.requestUse(index, state)
        }
        super.close()
    }

    override fun shouldPause(): Boolean = false

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            close()
            return true
        } else if (button == 1) {
            selectingSlot = null
            close()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }
}