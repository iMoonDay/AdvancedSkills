package com.imoonday.screen

import com.imoonday.config.UIConfigModel
import com.imoonday.render.SkillSlotRenderer
import com.imoonday.util.translate
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.SlimSliderComponent
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

        if (!SkillSlotRenderer.progressStrings.filterNotNull().all { it.fixed }) {
            rootComponent.child(Components.slimSlider(SlimSliderComponent.Axis.HORIZONTAL).apply {
                horizontalSizing(Sizing.fill(25))
                min(0.25)
                max(5.0)
                value(UIConfigModel.instance.nameScrollRate)
                onChanged().subscribe {
                    UIConfigModel.instance.nameScrollRate = it
                }
            })
        }

        rootComponent.mouseDrag().subscribe { mouseX, mouseY, deltaX, deltaY, button ->
            return@subscribe if (button == 0) {
                UIConfigModel.instance.uiOffsetX = client!!.window.scaledWidth - mouseX.toInt() - 32
                UIConfigModel.instance.uiOffsetY = mouseY.toInt() - client!!.window.scaledHeight / 2
                true
            } else {
                false
            }
        }

        rootComponent.mouseDown().subscribe { mouseX, mouseY, button ->
            return@subscribe if (button == 1) {
                UIConfigModel.instance.uiOffsetX = 0
                UIConfigModel.instance.uiOffsetY = 0
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
                UIConfigModel.instance.uiOffsetX += amount
            }

            GLFW.GLFW_KEY_RIGHT -> {
                UIConfigModel.instance.uiOffsetX -= amount
            }

            GLFW.GLFW_KEY_UP -> {
                UIConfigModel.instance.uiOffsetY -= amount
            }

            GLFW.GLFW_KEY_DOWN -> {
                UIConfigModel.instance.uiOffsetY += amount
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}