package com.imoonday.screen

import com.imoonday.components.equippedSkills
import com.imoonday.config.UIConfig
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.DraggableContainer
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class SkillSlotScreen : BaseOwoScreen<FlowLayout>() {
    val draggable: DraggableContainer<FlowLayout> = Containers.draggable(
        Sizing.content(),
        Sizing.content(),
        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            MinecraftClient.getInstance()?.player?.equippedSkills?.forEach {
                child(Components.texture(it.icon, 0, 0, 16, 16, 16, 16))
            }
        }
    ).apply {
        foreheadSize(0)
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    private val styleButton = Components.button(Text.literal("Style")) {
        UIConfig.instance.newStyle = !UIConfig.instance.newStyle
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.RIGHT)
            .verticalAlignment(VerticalAlignment.BOTTOM)

        rootComponent.child(styleButton)

        rootComponent.child(draggable.apply {
            rootComponent.mouseDrag().subscribe { mouseX, mouseY, deltaX, deltaY, button ->
                val result = this@apply.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button)
                fixPosition()
                result
            }
        })

    }

    override fun init() {
        super.init()
        draggable.apply {
            onMouseDrag(
                0.0,
                0.0,
                -client!!.window.scaledWidth * (1 - UIConfig.instance.slotXScaling),
                -client!!.window.scaledHeight * (1 - UIConfig.instance.slotYScaling),
                0
            )
        }
        styleButton.apply {
            updateX(2)
            updateY(2)
        }
    }

    override fun close() {
        save()
        super.close()
    }

    private fun save() {
        UIConfig.instance.slotXScaling = (draggable.x().coerceIn(
            0,
            client!!.window.scaledWidth - draggable.width()
        ) + draggable.width()) / client!!.window.scaledWidth.toDouble()
        UIConfig.instance.slotYScaling =
            ((draggable.y().coerceIn(
                0,
                client!!.window.scaledHeight - draggable.height()
            )) + draggable.height()) / client!!.window.scaledHeight.toDouble()
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        save()
        super.resize(client, width, height)
        client!!.setScreen(SkillSlotScreen())
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val amount = if (hasShiftDown()) 10.0 else 1.0
        when (keyCode) {
            GLFW.GLFW_KEY_LEFT -> {
                draggable.onMouseDrag(0.0, 0.0, -amount, 0.0, 0)
            }

            GLFW.GLFW_KEY_RIGHT -> {
                draggable.onMouseDrag(0.0, 0.0, amount, 0.0, 0)
            }

            GLFW.GLFW_KEY_UP -> {
                draggable.onMouseDrag(0.0, 0.0, 0.0, -amount, 0)
            }

            GLFW.GLFW_KEY_DOWN -> {
                draggable.onMouseDrag(0.0, 0.0, 0.0, amount, 0)
            }
        }
        fixPosition()
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun tick() {
        super.tick()
        fixPosition()
    }

    private fun fixPosition() {
        while (draggable.x() < 0) draggable.onMouseDrag(0.0, 0.0, 1.0, 0.0, 0)
        while (draggable.x() > client!!.window.scaledWidth - draggable.width()) draggable.onMouseDrag(
            0.0,
            0.0,
            -1.0,
            0.0,
            0
        )
        while (draggable.y() < 0) draggable.onMouseDrag(0.0, 0.0, 0.0, 1.0, 0)
        while (draggable.y() > client!!.window.scaledHeight - draggable.height()) draggable.onMouseDrag(
            0.0,
            0.0,
            0.0,
            -1.0,
            0
        )
    }
}