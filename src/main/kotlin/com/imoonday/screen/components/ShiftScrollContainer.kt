package com.imoonday.screen.components

import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.gui.screen.Screen.hasShiftDown

/**
 * A ScrollContainer that only scrolls when shift is held
 */
class ShiftScrollContainer<C : Component>(
    scrollDirection: ScrollDirection,
    horizontalSizing: Sizing?,
    verticalSizing: Sizing?,
    child: C,
) : ScrollContainer<C>(
    scrollDirection, horizontalSizing, verticalSizing, child
) {

    override fun onMouseScroll(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        return if (hasShiftDown() && maxScroll > 0) super.onMouseScroll(mouseX, mouseY, amount) else false
    }

    companion object {
        fun <C : Component> verticalScroll(
            horizontalSizing: Sizing?,
            verticalSizing: Sizing?,
            child: C,
        ): ScrollContainer<C> {
            return ShiftScrollContainer(ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child)
        }

        fun <C : Component> horizontalScroll(
            horizontalSizing: Sizing?,
            verticalSizing: Sizing?,
            child: C,
        ): ScrollContainer<C> {
            return ShiftScrollContainer(ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child)
        }
    }
}