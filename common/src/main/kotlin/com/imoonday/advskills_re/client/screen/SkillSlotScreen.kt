package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import org.lwjgl.glfw.*

class SkillSlotScreen : Screen(translate("screen.slot.title")) {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215)
        super.render(context, mouseX, mouseY, delta)
        SkillSlotRenderer.render(context)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        return if (button == 0) {
            val layout = SkillSlotRenderer.getValidLayout(clientPlayer!!.skillContainer.slotSize)
            if (layout.isEmpty()) return false
            val config = ClientConfig.get()
            config.uiOffsetX = client!!.window.scaledWidth - mouseX.toInt() - 18 * layout.maxOf { it.size } - 2
            config.uiOffsetY = mouseY.toInt() - client!!.window.scaledHeight / 2 + (9 * layout.size) + 2
            true
        } else false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (button == 1) {
            val config = ClientConfig.get()
            config.uiOffsetX = 0
            config.uiOffsetY = 0
            true
        } else false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val amount = if (hasShiftDown()) 10 else 1
        val config = ClientConfig.get()
        when (keyCode) {
            GLFW.GLFW_KEY_LEFT, client!!.options.leftKey.keyCode -> {
                config.uiOffsetX += amount
            }

            GLFW.GLFW_KEY_RIGHT, client!!.options.rightKey.keyCode -> {
                config.uiOffsetX -= amount
            }

            GLFW.GLFW_KEY_UP, client!!.options.forwardKey.keyCode -> {
                config.uiOffsetY -= amount
            }

            GLFW.GLFW_KEY_DOWN, client!!.options.backKey.keyCode -> {
                config.uiOffsetY += amount
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}