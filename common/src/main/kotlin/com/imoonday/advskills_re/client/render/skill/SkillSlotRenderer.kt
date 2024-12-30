package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.client.screen.SkillWheelScreen.Companion.quickCastSlot
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import kotlinx.serialization.*
import net.minecraft.client.gui.*
import net.minecraft.client.network.*
import net.minecraft.entity.player.*
import net.minecraft.text.*
import net.minecraft.util.math.*

private const val SLOT_SIZE_WITH_GAP = 22

object SkillSlotRenderer {

    private val slotsTexture = id("slots.png")
    private val config = ClientConfig.get()

    private var animationTime: Long = 250L
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

        val startX = getStartX(context, layout, config)
        val startY = getStartY(context, layout, config)
        val width = getTotalWidth(layout)
        val height = getTotalHeight(layout)
        context.enableScissor(startX, startY, startX + width, startY + height)

        val direction = config.dynamicallyHideDirection
        player.skillContainer.getAllSlots().forEach { slot ->
            val (x, y) = calculateXY(
                context,
                layout,
                slot.index
            )?.let { direction.handleOffset(it.first, it.second, offset) } ?: return@forEach
            renderSlot(context, x - 2, y - 2, slot.index == quickCastSlot)
            SkillRenderer.render(slot.skill, context, x, y, player, 0)
        }

        context.disableScissor()
    }

    private fun handleDynamicHide(player: PlayerEntity, layout: Array<IntArray>) {
        val currentTime = System.currentTimeMillis()
        val maxXOffset = getMaxXOffset(layout)

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

    private fun shouldDisplay(player: PlayerEntity): Boolean {
        return when (config.hideSkillSlots) {
            HideMode.HIDE -> return false
            HideMode.SHOW -> return true
            HideMode.DYNAMICALLY_HIDE ->
                player.equippedSkills.any { player.isUsing(it) || player.isCooling(it) }
                    || client?.currentScreen?.let { it is SkillWheelScreen || it is SkillSlotScreen } == true
                    || System.currentTimeMillis() - lastUseTime < 1000
        }
    }

    private fun getMaxXOffset(layout: Array<IntArray>): Int {
        val direction = config.dynamicallyHideDirection
        if (direction == AnimationDirection.UP || direction == AnimationDirection.DOWN) {
            return getTotalHeight(layout)
        }
        return getTotalWidth(layout)
    }

    private fun getTotalWidth(layout: Array<IntArray>): Int {
        if (layout.isEmpty()) return 0
        return SLOT_SIZE_WITH_GAP * layout.maxOf { it.size }
    }

    private fun getTotalHeight(layout: Array<IntArray>): Int {
        if (layout.isEmpty()) return 0
        return SLOT_SIZE_WITH_GAP * layout.size
    }

    private fun getStartY(
        context: DrawContext,
        layout: Array<IntArray>,
        config: ClientConfig
    ) = context.scaledWindowHeight / 2 - ((layout.size / 2.0) * SLOT_SIZE_WITH_GAP).toInt() + 1 + config.uiOffsetY

    private fun getStartX(
        context: DrawContext,
        layout: Array<IntArray>,
        config: ClientConfig
    ) = context.scaledWindowWidth - SLOT_SIZE_WITH_GAP * layout.maxOf { it.size } + 1 - config.uiOffsetX

    private fun calculateXY(
        context: DrawContext,
        layout: Array<IntArray>,
        index: Int,
    ): Pair<Int, Int>? {
        val (x, y) = findPosition(layout, index) ?: return null
        val startX =
            context.scaledWindowWidth - SLOT_SIZE_WITH_GAP * (layout.maxOf { it.size } - x + 1) - config.uiOffsetX + 3
        val startY =
            context.scaledWindowHeight / 2 + ((y - 1 - layout.size / 2.0) * SLOT_SIZE_WITH_GAP).toInt() + config.uiOffsetY + 3
        return Pair(startX, startY)
    }

    @JvmStatic
    fun getValidLayout(maxIndex: Int): Array<IntArray> =
        config.layout
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

        if (!config.displaySelectedSkillSlot) return

        val belowCrosshair =
            config.displayProgressBarBelowCrosshair && client?.entityRenderDispatcher?.camera?.isThirdPerson != true

        val slotSize = 22
        val (x, y) = config.selectedSlotPosition.getPosition(
            player,
            context.scaledWindowWidth,
            context.scaledWindowHeight,
            slotSize, slotSize
        )
        val skill = quickCastSlot?.let { player.getSkill(it) } ?: Skills.EMPTY

        renderSelectedSlot(context, x, y)
        SkillRenderer.render(skill, context, x + 3, y + 3, player, 0, belowCrosshair)

        if (config.displayQuickCastKey) {
            val text = "[".toText().append(ModKeyBindings.QUICK_CAST.boundKeyLocalizedText).append("]")
            val textRenderer = client!!.textRenderer
            context.drawCenteredTextWithShadow(
                textRenderer,
                text,
                x + 11,
                y - textRenderer.fontHeight,
                0xFFFFFF
            )
        }

        if (belowCrosshair) {
            SkillRenderer.renderProgressBar(
                skill,
                context,
                context.scaledWindowWidth / 2 - 8,
                context.scaledWindowHeight / 2 + 16 + config.progressBarOffsetY,
                16,
                1,
                player
            )
        }
    }

    @JvmStatic
    fun renderSelectedSlot(context: DrawContext, x: Int, y: Int) {
        RenderSystem.enableBlend()
        context.drawTexture(slotsTexture, x, y, 22, 64, 22, 22)
        RenderSystem.disableBlend()
    }

    @JvmStatic
    fun renderSlot(context: DrawContext, x: Int, y: Int, selected: Boolean) {
        RenderSystem.enableBlend()
        context.drawTexture(slotsTexture, x, y, if (selected) 68 else 48, 64, 20, 20)
        RenderSystem.disableBlend()
    }

    @Serializable
    enum class AnimationDirection {

        @SerialName("up")
        UP {

            override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x to y - offset
        },

        @SerialName("down")
        DOWN {

            override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x to y + offset
        },

        @SerialName("left")
        LEFT {

            override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x - offset to y
        },

        @SerialName("right")
        RIGHT {

            override fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int> = x + offset to y
        };

        val displayName: Text = translate("animationDirection.${name.lowercase()}")

        abstract fun handleOffset(x: Int, y: Int, offset: Int): Pair<Int, Int>
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
            ): Pair<Int, Int> = config.selectedSlotOffsetX to windowHeight - slotHeight + config.selectedSlotOffsetY
        },

        @SerialName("left_of_hotbar")
        LEFT_OF_HOTBAR {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                windowWidth / 2 - 91 - 29 - (if (player.offHandStack.isEmpty) 0 else slotWidth + 7) + config.selectedSlotOffsetX to windowHeight - slotHeight + config.selectedSlotOffsetY
        },

        @SerialName("right_of_hotbar")
        RIGHT_OF_HOTBAR {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                windowWidth / 2 + 91 + 29 - slotWidth + config.selectedSlotOffsetX to windowHeight - slotHeight + config.selectedSlotOffsetY
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
                windowWidth - slotWidth + config.selectedSlotOffsetX to windowHeight - slotHeight + config.selectedSlotOffsetY
        },

        @SerialName("center")
        CENTER {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                (windowWidth - slotWidth) / 2 + config.selectedSlotOffsetX to (windowHeight - slotHeight) / 2 + config.selectedSlotOffsetY
        },

        @SerialName("left_center")
        LEFT_CENTER {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                config.selectedSlotOffsetX to (windowHeight - slotHeight) / 2 + config.selectedSlotOffsetY
        },

        @SerialName("right_center")
        RIGHT_CENTER {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                windowWidth - slotWidth + config.selectedSlotOffsetX to (windowHeight - slotHeight) / 2 + config.selectedSlotOffsetY
        },

        @SerialName("top_center")
        TOP_CENTER {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                (windowWidth - slotWidth) / 2 + config.selectedSlotOffsetX to config.selectedSlotOffsetY
        },

        @SerialName("left_top")
        LEFT_TOP {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> = config.selectedSlotOffsetX to config.selectedSlotOffsetY
        },

        @SerialName("right_top")
        RIGHT_TOP {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> = windowWidth - slotWidth + config.selectedSlotOffsetX to config.selectedSlotOffsetY
        },

        @SerialName("custom")
        CUSTOM {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> = config.selectedSlotOffsetX to config.selectedSlotOffsetY
        },

        @SerialName("custom_percent")
        CUSTOM_PERCENT {

            override fun getPosition(
                player: ClientPlayerEntity,
                windowWidth: Int,
                windowHeight: Int,
                slotWidth: Int,
                slotHeight: Int
            ): Pair<Int, Int> =
                ((windowWidth - slotWidth) * (config.selectedSlotOffsetX / 100.0)).toInt() to ((windowHeight - slotHeight) * (config.selectedSlotOffsetY / 100.0)).toInt()
        };

        val displayName: Text = translate("slotPosition.${name.lowercase()}")

        abstract fun getPosition(
            player: ClientPlayerEntity,
            windowWidth: Int,
            windowHeight: Int,
            slotWidth: Int,
            slotHeight: Int
        ): Pair<Int, Int>
    }

    @Serializable
    enum class HideMode {

        @SerialName("show")
        SHOW,

        @SerialName("dynamically_hide")
        DYNAMICALLY_HIDE,

        @SerialName("hide")
        HIDE;

        val displayName: Text = translate("hideMode.${name.lowercase()}")
    }
}