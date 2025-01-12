package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.SkillRenderer.renderCooldownOverlay
import com.imoonday.advskills_re.client.render.skill.SkillRenderer.renderIcon
import com.imoonday.advskills_re.client.render.skill.SkillRenderer.renderProgressBar
import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.client.screen.SkillWheelScreen.Companion.quickCastSlot
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.gui.*
import net.minecraft.entity.player.*
import net.minecraft.util.*
import net.minecraft.util.math.*
import kotlin.Pair

private const val SLOT_SIZE_WITH_GAP = 22

object SkillSlotRenderer {

    private val widgetsTexture = Identifier("textures/gui/widgets.png")
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

        val layout = getValidLayout(player.skillContainer.slotSize)
        if (layout.isEmpty()) return

        handleDynamicHide(player, layout)

        val startX = getStartX(context, layout)
        val startY = getStartY(context, layout)
        val width = getTotalWidth(layout)
        val height = getTotalHeight(layout)
        context.enableScissor(startX, startY, startX + width, startY + height)

        val direction = config.dynamicallyHideDirection
        player.skillContainer.getAllSlots().forEach { slot ->
            val (x, y) = calculateXY(context, layout, slot.index)?.let {
                direction.handleOffset(
                    it.first, it.second, offset
                )
            } ?: return@forEach
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

    private fun shouldDisplay(player: PlayerEntity): Boolean =
        when (config.hideSkillSlots) {
            HideMode.HIDE -> false
            HideMode.SHOW -> true
            HideMode.DYNAMICALLY_HIDE -> shouldDisplayDynamically(player)
        }

    private fun shouldDisplayDynamically(player: PlayerEntity) = ((player.equippedSkills.any {
        if (player.isCooling(it)) {
            true
        } else if (it is UsingProgressTrigger) {
            it.isInUsingState(player)
        } else if (it is PassiveSkill) {
            !it.isToggleable(player) && player.isUsing(it)
        } else {
            player.isUsing(it)
        }
    } || (client?.currentScreen?.let { it is SkillWheelScreen || it is SkillSlotScreen } == true)
        || ((System.currentTimeMillis() - lastUseTime) < 1000)))

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
        layout: Array<IntArray>
    ) = context.scaledWindowHeight / 2 - ((layout.size / 2.0) * SLOT_SIZE_WITH_GAP).toInt() + 1 + config.uiOffsetY

    private fun getStartX(
        context: DrawContext,
        layout: Array<IntArray>
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
            player, context.scaledWindowWidth, context.scaledWindowHeight, slotSize, slotSize
        )
        val skill = quickCastSlot?.let { player.getSkill(it) } ?: Skills.EMPTY

        renderSelectedSlot(context, x, y) { iconX, iconY ->
            val endY = iconY + 16

            renderIcon(skill, context, iconX, iconY, player)
            if (!belowCrosshair) {
                renderProgressBar(skill, context, iconX, endY - 1, 16, 2, player)
            }
            renderCooldownOverlay(skill, context, iconX - 1, endY + 1, 18, 18, player)
        }

        if (config.displayQuickCastKey) {
            val text = "[".toText().append(ModKeyBindings.QUICK_CAST.boundKeyLocalizedText).append("]")
            val textRenderer = client!!.textRenderer
            val left = x + 11 < context.scaledWindowWidth / 2
            val centerX = if (left) x + 11 else x + 7 + 11
            context.drawCenteredTextWithShadow(
                textRenderer, text,
                centerX, y - textRenderer.fontHeight, 0xFFFFFF
            )
        }

        if (belowCrosshair) {
            renderProgressBar(
                skill, context, context.scaledWindowWidth / 2 - 8,
                context.scaledWindowHeight / 2 + 16 + config.progressBarOffsetY, 16, 1, player
            )
        }
    }

    @JvmStatic
    fun renderSelectedSlot(
        context: DrawContext,
        x: Int,
        y: Int,
        renderBelowOutline: (iconX: Int, iconY: Int) -> Unit = { _, _ -> }
    ) {
        RenderSystem.enableBlend()
        val i = context.scaledWindowWidth / 2
        val left = x + 11 < i
        if (config.useVanillaSlot) {
            if (left) {
                context.drawTexture(widgetsTexture, x, context.scaledWindowHeight - 23, 24, 22, 29, 24)
                renderBelowOutline(x + 3, y + 3)
            } else {
                context.drawTexture(widgetsTexture, x, context.scaledWindowHeight - 23, 53, 22, 29, 24)
                renderBelowOutline(x + 10, y + 3)
            }
        } else {
            val x1 = if (left) x else x + 7
            context.drawTexture(slotsTexture, x1, y, 22, 144, 22, 22)
            renderBelowOutline(x1 + 3, y + 3)
            context.drawTexture(slotsTexture, x1, y, 0, 144, 22, 22)
        }
        RenderSystem.disableBlend()
    }

    @JvmStatic
    fun renderSlot(context: DrawContext, x: Int, y: Int, selected: Boolean) {
        RenderSystem.enableBlend()
        context.drawTexture(slotsTexture, x, y, if (selected) 68 else 48, 64, 20, 20)
        RenderSystem.disableBlend()
    }
}