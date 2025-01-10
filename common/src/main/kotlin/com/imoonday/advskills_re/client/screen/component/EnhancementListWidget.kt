package com.imoonday.advskills_re.client.screen.component

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.narration.*
import net.minecraft.client.gui.widget.*
import net.minecraft.entity.player.*
import net.minecraft.text.*
import org.lwjgl.glfw.*
import java.util.function.*

class EnhancementListWidget(
    val client: MinecraftClient,
    private var x: Int,
    private var y: Int,
    private val width: Int,
    private val height: Int,
    private val player: PlayerEntity,
    skill: Skill,
    private val onClose: (EnhancementListWidget) -> Unit = { it.visible = false }
) : Widget, Drawable, Element {

    private val textRenderer = client.textRenderer
    var skill = skill
        private set
    var visible: Boolean = true
    private var focused: Boolean = false
    private var enhancements: Map<Enhancement, EnhancementData> = player.getEnhancements(skill)
    private var selected: Enhancement? = null
    private val selectedData: EnhancementData? get() = selected?.let { enhancements[it] }

    private val widgets: MutableList<ClickableWidget> = mutableListOf()

    private val scrollBar: ScrollableList =
        ScrollableList(x + 5, y + 5, width / 2 - 10, height - 10)
            .also { widgets.add(it) }
    private val levelUpButton: ButtonIconWidget = ButtonIconWidget(
        x + width - 7 - 20,
        y + height - 11 - 10,
        7, 11, selectionTexture, selectionTexture
    ).setTextureSize(256, 256)
        .setTextureOffset(0f, 0f)
        .setHoveredTextureOffset(0f, 16f)
        .addClickAction(0) { levelUp() }
        .apply { visible = canLevelUp() }
        .also { widgets.add(it) }

    private val levelDownButton: ButtonIconWidget = ButtonIconWidget(
        x + width / 2 + 20,
        y + height - 11 - 10,
        7, 11, selectionTexture, selectionTexture
    ).setTextureSize(256, 256)
        .setTextureOffset(16f, 0f)
        .setHoveredTextureOffset(16f, 16f)
        .addClickAction(0) { levelDown() }
        .apply { visible = canLevelDown() }
        .also { widgets.add(it) }

    private val onOffButton: ButtonIconWidget = ButtonIconWidget(
        levelDownButton.x + levelDownButton.width + (levelUpButton.x - levelDownButton.x - levelDownButton.width - 31) / 2,
        levelUpButton.y,
        31, 11, onOffTexture, onOffTexture
    ).setTextureSize(256, 256)
        .setTextureOffset(0f, 0f)
        .setHoveredTextureOffset(0f, 16f)
        .addClickAction(0) {
            val enhancement = selected ?: return@addClickAction
            val data = enhancements[enhancement] ?: return@addClickAction
            if (data.activated) {
                deactivate()
                it.setTextureOffset(32f, 0f)
                it.setHoveredTextureOffset(32f, 16f)
            } else {
                activate()
                it.setTextureOffset(0f, 0f)
                it.setHoveredTextureOffset(0f, 16f)
            }
        }
        .apply {
            visible = selected != null
            selectedData?.let {
                if (!it.activated) {
                    setTextureOffset(32f, 0f)
                    setHoveredTextureOffset(32f, 16f)
                }
            }
        }
        .also { widgets.add(it) }

    fun updateSkill(skill: Skill) {
        this.skill = skill
        this.scrollBar.resetOffset()
        update()
    }

    private fun canLevelUp() = selectedData != null && selectedData!!.currentLevel < selectedData!!.maxLevel

    private fun canLevelDown() = selectedData != null && selectedData!!.currentLevel > 1

    private fun levelUp(entry: Enhancement? = null) {
        sendRequest(ModifyEnhancementC2SRequest.Operation.LEVEL_UP, entry)
    }

    private fun levelDown(entry: Enhancement? = null) {
        sendRequest(ModifyEnhancementC2SRequest.Operation.LEVEL_DOWN, entry)
    }

    private fun activate(entry: Enhancement? = null) {
        sendRequest(ModifyEnhancementC2SRequest.Operation.ACTIVATE, entry)
    }

    private fun deactivate(entry: Enhancement? = null) {
        sendRequest(ModifyEnhancementC2SRequest.Operation.DEACTIVATE, entry)
    }

    private fun sendRequest(operation: ModifyEnhancementC2SRequest.Operation, entry: Enhancement? = null) {
        (entry ?: selected)?.let {
            Channels.MODIFY_ENHANCEMENT_C2S.sendToServer(
                ModifyEnhancementC2SRequest(skill, it.id, operation)
            )
        }
    }

    fun close() {
        onClose(this)
    }

    fun restoreFrom(widget: EnhancementListWidget, windowSize: Pair<Int, Int>? = null) {
        this.visible = widget.visible
        this.focused = widget.focused
        this.skill = widget.skill
        this.enhancements = widget.enhancements
        this.selected = widget.selected
        this.scrollBar.offset = widget.scrollBar.offset
        this.setPosition(widget.x, widget.y)
        windowSize?.let { this.fixOverflow(it.first, it.second) }
        updateButtons()
    }

    override fun setX(x: Int) {
        this.x = x
        this.scrollBar.x = x + 5
        this.levelUpButton.x = x + width - 7 - 20
        this.levelDownButton.x = x + width / 2 + 20
        this.onOffButton.x =
            levelDownButton.x + levelDownButton.width + (levelUpButton.x - levelDownButton.x - levelDownButton.width - onOffButton.width) / 2
    }

    override fun setY(y: Int) {
        this.y = y
        this.scrollBar.y = y + 5
        this.levelUpButton.y = y + height - 11 - 10
        this.levelDownButton.y = y + height - 11 - 10
        this.onOffButton.y = levelUpButton.y
    }

    override fun getX(): Int = x

    override fun getY(): Int = y

    override fun getWidth(): Int = width

    override fun getHeight(): Int = height

    override fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    override fun isFocused(): Boolean = focused

    override fun getNavigationFocus(): ScreenRect = ScreenRect(x, y, width, height)

    override fun forEachChild(consumer: Consumer<ClickableWidget>) = widgets.forEach(consumer)

    fun update() {
        this.enhancements = player.getEnhancements(skill)
        if (this.selected !in enhancements) {
            this.selected = enhancements.keys.firstOrNull()
        }

        updateButtons()
    }

    private fun updateButtons() {
        levelUpButton.visible = canLevelUp()
        levelDownButton.visible = canLevelDown()
        onOffButton.visible = selected != null
        selectedData?.let {
            onOffButton.setTextureOffset(if (it.activated) 0f else 32f, 0f)
            onOffButton.setHoveredTextureOffset(if (it.activated) 0f else 32f, 16f)
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) return

        val matrices = context.matrices

        matrices.push()
        matrices.translate(0f, 0f, 100f)
        renderBackground(context, mouseX, mouseY)
        widgets.forEach { it.render(context, mouseX, mouseY, delta) }

        val centerX = x + width / 2 + width / 4
        val centerY = y + height / 2
        val maxWidth = width / 2 - 20

        val name = skill.name
        context.drawScaledText(textRenderer, name, maxWidth, centerX, y + 10, 0xFFFFFF)

        val iconX = x + width - 5 - 16
        val iconY = y + 5
        SkillRenderer.renderIcon(skill, context, iconX, iconY)
        if (mouseX in iconX..(iconX + 16) && mouseY in iconY..(iconY + 16)) {
            SkillRenderer.renderTooltip(client, skill, context, mouseX, mouseY, player)
        }

        if (enhancements.isEmpty()) {
            val text = translate("widget.enhancement_list.empty")
            context.drawText(
                textRenderer,
                text,
                x + width / 4 - textRenderer.getWidth(text) / 2,
                y + height / 2 - textRenderer.fontHeight / 2,
                0xFFFFFF,
                false
            )
        } else {
            do {
                val enhancement = selected ?: break
                val data = selectedData ?: break

                context.drawScaledText(textRenderer, enhancement.name, maxWidth, centerX, centerY - 20, 0xFFFFFF)

                val level = data.currentLevel
                context.drawScaledText(
                    textRenderer, translate("widget.enhancement_list.level", level, data.maxLevel),
                    maxWidth, centerX, centerY - 10, 0xFFFFFF
                )

                skill.getEnhancementTooltip(enhancement.id, level)?.let { descText ->
                    context.drawScaledText(textRenderer, descText, maxWidth, centerX, centerY, 0xFFFFFF)
                }

                if (!data.activated) {
                    context.drawCenteredTextWithShadow(
                        textRenderer, translate("widget.enhancement_list.deactivated"),
                        centerX, centerY + 20, 0xFF0000
                    )
                }
            } while (false)
        }

        matrices.pop()
    }

    private fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int) {
//        context.renderVanillaTranslucent(0, 0, context.scaledWindowWidth, context.scaledWindowHeight)
        context.renderPanel(x, y, width / 2, height)
        context.renderDarkPanel(x + width / 2, y, width / 2, height)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible) return false

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close()
            return true
        }

        val pressed = widgets.any { it.keyPressed(keyCode, scanCode, modifiers) }
        if (pressed) {
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible) return false

        val released = widgets.any { it.keyReleased(keyCode, scanCode, modifiers) }
        if (released) {
            return true
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible) return false

        if (button == 1) {
            close()
            return true
        }

        for (widget in widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                widget.isFocused = true
                return true
            } else {
                widget.isFocused = false
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (!visible) return false

        val dragged = widgets.any { it.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) }
        if (dragged) {
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (!visible) return

        widgets.forEach { it.mouseMoved(mouseX, mouseY) }
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible) return false

        val released = widgets.any { it.mouseReleased(mouseX, mouseY, button) }
        if (released) {
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if (!visible) return false

        val scrolled = widgets.any { it.mouseScrolled(mouseX, mouseY, amount) }
        if (scrolled) {
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        if (!visible) return false

        val moveOver = mouseX.toInt() in x..(x + width) && mouseY.toInt() in y..(y + height)
        if (moveOver) {
            return true
        }

        return super.isMouseOver(mouseX, mouseY)
    }

    fun fixOverflow(screenWidth: Int, screenHeight: Int) {
        if (this.x + this.width > screenWidth) {
            this.setX(screenWidth - this.width)
        }
        if (this.x < 0) {
            this.setX(0)
        }

        if (this.y + this.height > screenHeight) {
            this.setY(screenHeight - this.height)
        }
        if (this.y < 0) {
            this.setY(0)
        }
    }

    inner class ScrollableList(x: Int, y: Int, width: Int, height: Int, private val visibleCount: Int = 5) :
        ClickableWidget(x, y, width, height, Text.empty()) {

        var offset: Int = 0
        private val entryHeight
            get() = height / visibleCount
        private var lastClickTime: Long = 0L
        private var lastClickedEnhancement: Enhancement? = null

        fun resetOffset() {
            offset = 0
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (!isMouseOver(mouseX, mouseY) || button != 0) return false

            val clickedIndex = ((mouseY.toInt() - y) / entryHeight) + offset
            val enhancement = enhancements.keys.elementAtOrNull(clickedIndex) ?: return false

            if (Screen.hasShiftDown()) {
                toggleActivation(enhancement)
            } else {
                val currentTime = System.currentTimeMillis()
                if (enhancement == lastClickedEnhancement && currentTime - lastClickTime < 250) {
                    toggleActivation(enhancement)
                    lastClickedEnhancement = null
                } else {
                    selected = enhancement
                    updateButtons()
                    lastClickedEnhancement = enhancement
                }
                lastClickTime = currentTime
            }
            return true
        }

        private fun toggleActivation(enhancement: Enhancement) {
            val data = enhancements[enhancement] ?: return
            if (data.activated) {
                deactivate(enhancement)
            } else {
                activate(enhancement)
            }
        }

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.drawBox(x, y, width, height, this.isFocused)

            val totalEntries = enhancements.size

            val endIndex = (offset + visibleCount).coerceAtMost(totalEntries)
            val entries = enhancements.entries.toList().subList(offset, endIndex)

            entries.forEachIndexed { index, entry ->
                val enhancement = entry.key
                val yPos = y + index * entryHeight

                if (mouseX.toInt() in x..<(x + width) && mouseY.toInt() in yPos..<(yPos + entryHeight)) {
                    context.fill(x + 1, yPos + 1, x + width - 1, yPos + entryHeight, 0x50FFFFFF)
                } else if (selected == enhancement) {
                    context.fill(x + 1, yPos + 1, x + width - 1, yPos + entryHeight, 0x25FFFFFF)
                }

                context.drawScrollableText(
                    textRenderer,
                    enhancement.name,
                    x + 2,
                    yPos,
                    x + width - 2,
                    yPos + entryHeight,
                    getColor(enhancement),
                    false
                )
            }
        }

        private fun getColor(enhancement: Enhancement): Int {
            val data = enhancements[enhancement] ?: return 0xFFFFFF
            return if (data.activated) {
                if (data.currentLevel == data.maxLevel) 0x00FF00 else 0xFFFF00
            } else {
                0xFF0000
            }
        }

        override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
            if (!isMouseOver(mouseX, mouseY)) return false

            val newOffset = (offset - amount.toInt()).coerceIn(0, (enhancements.size - visibleCount).coerceAtLeast(0))
            if (newOffset != offset) {
                offset = newOffset
                return true
            }
            return false
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) = Unit
    }

    companion object {

        private val selectionTexture = id("textures/gui/selection.png")
        private val onOffTexture = id("on_off.png")
    }
}
