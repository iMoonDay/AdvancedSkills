package com.imoonday.screen

import com.imoonday.config.Config
import com.imoonday.render.SkillSlotRenderer
import com.imoonday.utils.translate
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import org.lwjgl.glfw.GLFW

class SkillSlotScreen : BaseOwoScreen<FlowLayout>() {

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.TOP)
            .padding(Insets.top(5))

        rootComponent.child(
            Components.label(
                translate("screen", "slot.title")
            )
        )

        rootComponent.mouseDrag().subscribe { mouseX, mouseY, deltaX, deltaY, button ->
            return@subscribe if (button == 0) {
                Config.instance.offsetX = client!!.window.scaledWidth - mouseX.toInt() - 32
                Config.instance.offsetY = mouseY.toInt() - client!!.window.scaledHeight / 2
                true
            } else {
                false
            }
        }

        rootComponent.mouseDown().subscribe { mouseX, mouseY, button ->
            return@subscribe if (button == 1) {
                Config.instance.offsetX = 0
                Config.instance.offsetY = 0
                true
            } else {
                false
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        client?.let {
            SkillSlotRenderer.render(it, context, delta)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val amount = if (hasShiftDown()) 10 else 1
        when (keyCode) {
            GLFW.GLFW_KEY_LEFT -> {
                Config.instance.offsetX += amount
            }

            GLFW.GLFW_KEY_RIGHT -> {
                Config.instance.offsetX -= amount
            }

            GLFW.GLFW_KEY_UP -> {
                Config.instance.offsetY -= amount
            }

            GLFW.GLFW_KEY_DOWN -> {
                Config.instance.offsetY += amount
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}