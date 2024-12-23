package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.client.screen.SkillWheelScreen.Companion.quickCastSlot
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import kotlinx.serialization.*
import net.minecraft.client.gui.*
import net.minecraft.client.network.*
import net.minecraft.entity.player.*
import net.minecraft.util.math.*
import java.awt.*

object SkillSlotRenderer {

    private val bgColor = Color.DARK_GRAY.alpha(0.75).rgb

    private var animationTime: Long = 500
    private var offset: Int = 0
    private var startAnimationTime: Long? = null
    private var isExpanded: Boolean = false
    private var isExpanding: Boolean = false
    private var isFolding: Boolean = false
    var lastUseTime: Long = 0

    @JvmStatic
    fun render(context: DrawContext) {
        val player = clientPlayer ?: return
        if (player.isSpectator) return
        val layout =
            getValidLayout(player.skillContainer.slotSize)
        if (layout.isEmpty()) return

        handleDynamicHide(player, layout)

        val config = ClientConfig.get()
        val startX = getBgX(context, layout, config)
        val startY = getBgY(context, layout, config)
        val bgWidth = getBgWidth(layout)
        val bgHeight = getBgHeight(layout)
        context.enableScissor(startX, startY, startX + bgWidth, startY + bgHeight)

        val direction = config.hideEdge
        if (!config.hideSkillSlotBackground) {
            val (bgX, bgY) = direction.handlePosition(startX, startY, offset)
            context.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, bgColor)
        }

        player.skillContainer.getAllSlots().forEach { slot ->
            val (x, y) = calculateXY(
                context,
                layout,
                slot.index
            )?.let { direction.handlePosition(it.first, it.second, offset) } ?: return@forEach
            SkillRenderer.render(slot.skill, context, x, y, player)
            if (slot.index == quickCastSlot) {
                context.drawBorder(x - 1, y - 1, 18, 18, 0xFF00FF00.toInt())
            }
        }

        context.disableScissor()
    }

    private fun handleDynamicHide(player: PlayerEntity, layout: Array<IntArray>) {
        val currentTime = System.currentTimeMillis()
        val maxXOffset = getMaxXOffset(layout)

        if (animationTime != 250L) {
            animationTime = 250L
        }

        if (shouldDisplay(player)) {
            if (!isExpanded) {
                if (isFolding && startAnimationTime != null) {
                    startAnimationTime = -startAnimationTime!! - animationTime + (currentTime) * 2
                    isFolding = false
                } else if (startAnimationTime == null) {
                    startAnimationTime = currentTime
                }

                val elapsedTime = currentTime - startAnimationTime!!

                val progress =
                    (elapsedTime.toFloat() / animationTime.toFloat()).coerceIn(0f, 1f)

                offset = MathHelper.lerp(progress, maxXOffset, 0)

                if (offset == 0) {
                    isExpanded = true
                    startAnimationTime = null
                    isExpanding = false
                } else {
                    isExpanding = true
                }
            }
        } else if (offset < maxXOffset) {
            if (isExpanding && startAnimationTime != null) {
                startAnimationTime = -startAnimationTime!! - animationTime + (currentTime) * 2
                isExpanding = false
            } else if (isExpanded && offset == 0 || startAnimationTime == null) {
                isExpanded = false
                startAnimationTime = currentTime
            }

            val elapsedTime = currentTime - startAnimationTime!!
            val progress = (elapsedTime.toFloat() / animationTime.toFloat()).coerceIn(0f, 1f)

            offset = MathHelper.lerp(progress, 0, maxXOffset)

            if (offset == maxXOffset) {
                startAnimationTime = null
                isFolding = false
            } else {
                isFolding = true
            }
        } else {
            startAnimationTime = null
        }
    }

    private fun shouldDisplay(player: PlayerEntity): Boolean =
        !ClientConfig.get().dynamicallyHideSkillSlots
            || player.equippedSkills.any {
            player.isUsing(it)
                || player.isCooling(it)
        }
            || client?.currentScreen?.let { it is SkillWheelScreen || it is SkillSlotScreen } == true
            || System.currentTimeMillis() - lastUseTime < 1000

    private fun getMaxXOffset(layout: Array<IntArray>): Int {
        val config = ClientConfig.get()
        val direction = config.hideEdge
        if (direction == AnimationDirection.UP || direction == AnimationDirection.DOWN) {
            return getBgHeight(layout)
        }
        return 18 * (layout.maxOfOrNull { it.size } ?: 0) + 2
    }

    private fun getBgWidth(layout: Array<IntArray>): Int {
        if (layout.isEmpty()) return 0
        return 18 * layout.maxOf { it.size } + 2
    }

    private fun getBgHeight(layout: Array<IntArray>): Int {
        if (layout.isEmpty()) return 0
        return 18 * layout.size + 2
    }

    private fun getBgY(
        context: DrawContext,
        layout: Array<IntArray>,
        config: ClientConfig
    ) = context.scaledWindowHeight / 2 - ((layout.size / 2.0) * 18).toInt() + config.uiOffsetY - 2

    private fun getBgX(
        context: DrawContext,
        layout: Array<IntArray>,
        config: ClientConfig
    ) = context.scaledWindowWidth - 18 * layout.maxOf { it.size } - config.uiOffsetX - 2

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

    @JvmStatic
    fun renderSelectedSkill(context: DrawContext) {
        val player = clientPlayer ?: return
        if (player.isSpectator) return

        val config = ClientConfig.get()
        if (!config.displaySelectedSkillSlot) return

        val slotSize = 18
        val (x, y) = config.selectedSlotPosition.getPosition(
            player,
            context.scaledWindowWidth,
            context.scaledWindowHeight,
            slotSize, slotSize
        )
        context.fill(x, y, x + slotSize, y + slotSize, bgColor)
        context.drawBorder(x, y, slotSize, slotSize, 0x88FFFFFF.toInt())

        val skill = quickCastSlot?.let { player.getSkill(it) } ?: Skills.EMPTY
        SkillRenderer.render(skill, context, x + 1, y + 1, player)

        val text = ModKeyBindings.QUICK_CAST.boundKeyLocalizedText
        val textRenderer = client!!.textRenderer
        context.drawCenteredTextWithShadow(
            textRenderer,
            text,
            x + 9,
            y - textRenderer.fontHeight / 2,
            0xFFFFFF
        )
    }

    @Serializable
    enum class AnimationDirection {

        @SerialName("up")
        UP {

            override fun handlePosition(x: Int, y: Int, offset: Int): Pair<Int, Int> = x to y - offset
        },

        @SerialName("down")
        DOWN {

            override fun handlePosition(x: Int, y: Int, offset: Int): Pair<Int, Int> = x to y + offset
        },

        @SerialName("left")
        LEFT {

            override fun handlePosition(x: Int, y: Int, offset: Int): Pair<Int, Int> = x - offset to y
        },

        @SerialName("right")
        RIGHT {

            override fun handlePosition(x: Int, y: Int, offset: Int): Pair<Int, Int> = x + offset to y
        };

        abstract fun handlePosition(x: Int, y: Int, offset: Int): Pair<Int, Int>
    }

    @Serializable
    enum class SlotPosition {

        @SerialName("left_bottom")
        LEFT_BOTTOM {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                0 to windowHeight - 18 - 2
        },

        @SerialName("left_of_inventory")
        LEFT_OF_INVENTORY {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                windowWidth / 2 - 91 - 26 - (if (player.offHandStack.isEmpty) 0 else slotWidth + 3 + 4) to windowHeight - slotHeight - 2
        },

        @SerialName("right_of_inventory")
        RIGHT_OF_INVENTORY {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                windowWidth / 2 + 91 + 26 - slotWidth to windowHeight - slotHeight - 2
        },

        @SerialName("right_bottom")
        RIGHT_BOTTOM {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                windowWidth - slotWidth - 2 to windowHeight - slotHeight - 2
        },

        @SerialName("custom")
        CUSTOM {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> {
                val config = ClientConfig.get()
                return config.selectedSlotOffsetX to config.selectedSlotOffsetY
            }
        };

        abstract fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int>
    }
}