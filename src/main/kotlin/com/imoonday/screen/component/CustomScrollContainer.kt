package com.imoonday.screen.component

import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.Sizing

class CustomScrollContainer<C : Component>(
    direction: ScrollDirection,
    horizontalSizing: Sizing,
    verticalSizing: Sizing,
    child: C,
) : ScrollContainer<C>(direction, horizontalSizing, verticalSizing, child) {

    fun scrollToInstantly(component: Component) {
        scrollTo(component)
        skipAnimation()
    }

    fun scrollToTop() {
        scrollTo(0.0)
        skipAnimation()
    }

    fun scrollToBottom() {
        scrollTo(1.0)
        skipAnimation()
    }

    fun skipAnimation() {
        currentScrollPosition = scrollOffset
    }

    companion object {

        fun <C : Component> vertical(
            horizontalSizing: Sizing,
            verticalSizing: Sizing,
            child: C,
        ): CustomScrollContainer<C> =
            CustomScrollContainer(ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child)

        fun <C : Component> horizontal(
            horizontalSizing: Sizing,
            verticalSizing: Sizing,
            child: C,
        ): CustomScrollContainer<C> =
            CustomScrollContainer(ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child)
    }
}