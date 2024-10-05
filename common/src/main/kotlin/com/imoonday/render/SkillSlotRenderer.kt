package com.imoonday.render

import com.imoonday.config.UIConfig
import com.imoonday.util.alpha
import com.imoonday.util.skillContainer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.awt.Color

object SkillSlotRenderer {

    fun render(client: MinecraftClient, context: DrawContext) {
        val player = client.player ?: return
        if (player.isSpectator) return
        val layout = getValidLayout(player.skillContainer.slotSize)
        renderBackground(context, layout)
        player.skillContainer.getAllSlots().forEach {
            val (x, y) = calculateXY(context, layout, it.index) ?: return@forEach
            it.skill.render(context, x, y, player)
        }
    }

    private fun renderBackground(
        context: DrawContext,
        layout: Array<IntArray>,
    ) {
        val x =
            context.scaledWindowWidth - 18 * layout.maxOf { it.size } - UIConfig.instance.uiOffsetX - 2
        val y =
            context.scaledWindowHeight / 2 - ((layout.size / 2.0) * 18).toInt() + UIConfig.instance.uiOffsetY - 2
        context.fill(
            x,
            y,
            x + 18 * layout.maxOf { it.size } + 2,
            y + 18 * layout.size + 2,
            Color.DARK_GRAY.alpha(0.75).rgb
        )
    }

    private fun calculateXY(
        context: DrawContext,
        layout: Array<IntArray>,
        index: Int,
    ): Pair<Int, Int>? {
        val (x, y) = findPosition(layout, index) ?: return null
        val startX =
            context.scaledWindowWidth - 18 * (layout.maxOf { it.size } - x + 1) - UIConfig.instance.uiOffsetX
        val startY =
            context.scaledWindowHeight / 2 + ((y - 1 - layout.size / 2.0) * 18).toInt() + UIConfig.instance.uiOffsetY
        return Pair(startX, startY)
    }

    fun getValidLayout(maxIndex: Int): Array<IntArray> =
        UIConfig.instance.layout
            .map { row ->
                row.map { if (it !in 0..maxIndex) 0 else it }.toIntArray()
            }
            .map { row ->
                row.toMutableList().dropLastWhile { it == 0 }.toIntArray()
            }
            .filterNot { it.isEmpty() }
            .toTypedArray()

    fun findPosition(layout: Array<IntArray>, number: Int): Pair<Int, Int>? {
        for (rowIndex in layout.indices) {
            val row = layout[rowIndex]
            for (colIndex in row.indices) {
                if (row[colIndex] == number) {
                    return colIndex + 1 to rowIndex + 1
                }
            }
        }
        return null
    }
}