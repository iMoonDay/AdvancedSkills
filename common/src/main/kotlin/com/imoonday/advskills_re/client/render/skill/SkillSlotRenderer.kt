package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.screen.SkillWheelScreen.Companion.quickCastSlot
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import java.awt.*

object SkillSlotRenderer {

    @JvmStatic
    fun render(context: DrawContext) {
        val player = clientPlayer ?: return
        if (player.isSpectator) return
        val layout =
            getValidLayout(player.skillContainer.slotSize)
        if (layout.isEmpty()) return
        renderBackground(context, layout)
        player.skillContainer.getAllSlots().forEach {
            val (x, y) = calculateXY(
                context,
                layout,
                it.index
            ) ?: return@forEach
            SkillRenderer.render(it.skill, context, x, y, player)
            if (it.index == quickCastSlot) {
                context.drawBorder(x - 1, y - 1, 18, 18, 0xFF00FF00.toInt())
            }
        }
    }

    private fun renderBackground(
        context: DrawContext,
        layout: Array<IntArray>,
    ) {
        val config = ClientConfig.get()
        val x =
            context.scaledWindowWidth - 18 * layout.maxOf { it.size } - config.uiOffsetX - 2
        val y =
            context.scaledWindowHeight / 2 - ((layout.size / 2.0) * 18).toInt() + config.uiOffsetY - 2
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
        val config = ClientConfig.get()
        val startX =
            context.scaledWindowWidth - 18 * (layout.maxOf { it.size } - x + 1) - config.uiOffsetX
        val startY =
            context.scaledWindowHeight / 2 + ((y - 1 - layout.size / 2.0) * 18).toInt() + config.uiOffsetY
        return Pair(startX, startY)
    }

    @JvmStatic
    fun getValidLayout(maxIndex: Int): Array<IntArray> =
        ClientConfig.get().layout
            .map { row ->
                row.map { if (it !in 0..maxIndex) 0 else it }.toIntArray()
            }
            .map { row ->
                row.toMutableList().dropLastWhile { it == 0 }.toIntArray()
            }
            .filterNot { it.isEmpty() }
            .toTypedArray()

    @JvmStatic
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