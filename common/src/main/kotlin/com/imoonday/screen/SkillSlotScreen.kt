package com.imoonday.screen

import com.imoonday.config.UIConfig
import com.imoonday.render.SkillSlotRenderer
import com.imoonday.util.clientPlayer
import com.imoonday.util.keyCode
import com.imoonday.util.skillContainer
import com.imoonday.util.translate
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

        rootComponent.mouseDrag().subscribe { mouseX, mouseY, _, _, button ->
            return@subscribe if (button == 0) {
                val layout = SkillSlotRenderer.getValidLayout(clientPlayer!!.skillContainer.slotSize)
                UIConfig.instance.uiOffsetX =
                    client!!.window.scaledWidth - mouseX.toInt() - 18 * layout.maxOf { it.size } - 2
                UIConfig.instance.uiOffsetY = mouseY.toInt() - client!!.window.scaledHeight / 2 + (9 * layout.size) + 2
                true
            } else {
                false
            }
        }

        rootComponent.mouseDown().subscribe { _, _, button ->
            return@subscribe if (button == 1) {
                UIConfig.instance.uiOffsetX = 0
                UIConfig.instance.uiOffsetY = 0
                true
            } else false
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        client?.let {
            SkillSlotRenderer.render(it, context)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val amount = if (hasShiftDown()) 10 else 1
        when (keyCode) {
            GLFW.GLFW_KEY_LEFT, client!!.options.leftKey.keyCode -> {
                UIConfig.instance.uiOffsetX += amount
            }

            GLFW.GLFW_KEY_RIGHT, client!!.options.rightKey.keyCode -> {
                UIConfig.instance.uiOffsetX -= amount
            }

            GLFW.GLFW_KEY_UP, client!!.options.forwardKey.keyCode -> {
                UIConfig.instance.uiOffsetY -= amount
            }

            GLFW.GLFW_KEY_DOWN, client!!.options.backKey.keyCode -> {
                UIConfig.instance.uiOffsetY += amount
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}