package com.imoonday.screen

import com.imoonday.config.UIConfig
import com.imoonday.render.SkillSlotRenderer
import com.imoonday.util.clientPlayer
import com.imoonday.util.skillContainer
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

        if (!UIConfig.instance.simplify && !SkillSlotRenderer.progressStrings.values.all { it.fixed }) {
            rootComponent.child(Components.slimSlider(SlimSliderComponent.Axis.HORIZONTAL).apply {
                horizontalSizing(Sizing.fill(25))
                min(0.25)
                max(5.0)
                value(UIConfig.instance.nameScrollRate)
                onChanged().subscribe {
                    UIConfig.instance.nameScrollRate = it
                }
            })
        }

        rootComponent.child(Components.smallCheckbox(translate("screen", "slot.simplify")).apply {
            checked(UIConfig.instance.simplify)
            onChanged().subscribe {
                UIConfig.instance.uiOffsetX = 0
                UIConfig.instance.uiOffsetY = 0
                UIConfig.instance.simplify = it
            }
            positioning(Positioning.relative(1, 1))
        })

        rootComponent.mouseDrag().subscribe { mouseX, mouseY, _, _, button ->
            return@subscribe if (button == 0) {
                if (UIConfig.instance.simplify) {
                    val slotSize = (clientPlayer?.skillContainer?.slotSize ?: 0)
                    val maxSlotSize = if (slotSize <= 5) slotSize else slotSize / 2 + if (slotSize % 2 == 0) 0 else 1
                    UIConfig.instance.uiOffsetX =
                        client!!.window.scaledWidth - mouseX.toInt() - (maxSlotSize / 2.0 * 18).toInt()
                    UIConfig.instance.uiOffsetY =
                        mouseY.toInt() - client!!.window.scaledHeight + if (slotSize > 5) 20 else 12
                } else {
                    UIConfig.instance.uiOffsetX = client!!.window.scaledWidth - mouseX.toInt() - 32
                    UIConfig.instance.uiOffsetY = mouseY.toInt() - client!!.window.scaledHeight / 2
                }
                true
            } else {
                false
            }
        }

        rootComponent.mouseDown().subscribe { mouseX, mouseY, button ->
            return@subscribe if (button == 1) {
                UIConfig.instance.uiOffsetX = 0
                UIConfig.instance.uiOffsetY = 0
                true
            } else {
                false
            }
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
            GLFW.GLFW_KEY_LEFT -> {
                UIConfig.instance.uiOffsetX += amount
            }

            GLFW.GLFW_KEY_RIGHT -> {
                UIConfig.instance.uiOffsetX -= amount
            }

            GLFW.GLFW_KEY_UP -> {
                UIConfig.instance.uiOffsetY -= amount
            }

            GLFW.GLFW_KEY_DOWN -> {
                UIConfig.instance.uiOffsetY += amount
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}