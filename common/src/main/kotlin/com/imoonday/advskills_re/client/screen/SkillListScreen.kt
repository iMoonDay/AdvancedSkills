package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.client.screen.component.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.SkillSlot.Companion.indexTexture
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.narration.*
import net.minecraft.client.gui.tooltip.*
import net.minecraft.client.gui.widget.*
import net.minecraft.client.network.*
import net.minecraft.client.sound.*
import net.minecraft.entity.player.*
import net.minecraft.text.*
import java.awt.*

class SkillListScreen(
    val player: PlayerEntity,
) : Screen(Text.empty()), Syncable {

    private var showCreativeButtons = false
    var selectedSkill: Skill? = null
    var selectedSlot: Int? = null
    private val selectedSlotSkill: Skill?
        get() = selectedSlot?.let(player::getSkill)
    private val skillSlots = mutableListOf<EquippedSkillSlot>()
    private val container
        get() = player.skillContainer
    private lateinit var enhancementList: EnhancementListWidget
    private lateinit var skillScroll: SkillContainerWidget
    private lateinit var learnButton: ButtonWidget

    override fun init() {
        super.init()
        initEnhancementList()
        showCreativeButtons = player.isCreative && player.hasPermissionLevel(4)
        addSkillScroll()
        addSkillSlots()
        if (showCreativeButtons) {
            val learnAllButton = createButton(
                5, height - 25,
                translate("screen.list.button.learnAll"),
                "learn-all"
            ).also(::addDrawableChild)
            val forgetAllButton = createButton(
                learnAllButton.x + learnAllButton.width + 5, height - 25,
                translate("screen.list.button.forgetAll"),
                "forget-all"
            ).also(::addDrawableChild)
            createButton(
                forgetAllButton.x + forgetAllButton.width + 5, height - 25,
                translate("screen.list.button.resetCooldown"),
                "reset-cooldown",
                true
            ).also(::addDrawableChild)
        }
        val inventoryText = translate("screen.list.button.inventory")
        val inventoryButtonWidth = (textRenderer.getWidth(inventoryText) + 10).coerceAtLeast(50)
        val inventoryButton = ButtonWidget.builder(inventoryText) {
            client!!.setScreen(SkillInventoryScreen(player) { SkillListScreen(player) })
        }.dimensions(width - 5 - inventoryButtonWidth, 5, inventoryButtonWidth, 20)
            .build()
            .also(::addDrawableChild)
        val learnText = if (!player.hasLearnedAll()) translate("screen.list.button.learn")
        else translate("screen.list.button.enhance")
        val learnButtonWidth = (textRenderer.getWidth(learnText) + 10).coerceAtLeast(50)
        learnButton = ButtonWidget.builder(learnText) {
            client!!.setScreen(SkillChoiceScreen(player) { SkillListScreen(player) })
        }.dimensions(inventoryButton.x - learnButtonWidth - 5, 5, learnButtonWidth, 20)
            .build()
            .apply { active = !player.choiceData.isEmpty() }
            .also(::addDrawableChild)
    }

    private fun initEnhancementList() {
        enhancementList = EnhancementListWidget(
            client!!, width / 2 - width / 4, height / 2 - height / 4, width / 2,
            height / 2, player, Skills.EMPTY
        ).apply {
            visible = false
            fixOverflow(this@SkillListScreen.width, this@SkillListScreen.height)
        }
    }

    private fun createButton(x: Int, y: Int, text: Text, content: String, close: Boolean = false) =
        ButtonWidget.builder(text) {
            (player as ClientPlayerEntity).networkHandler.sendCommand("skills $content @s")
            if (close) close()
        }.tooltip(Tooltip.of(translate("screen.list.button.tooltip")))
            .width((textRenderer.getWidth(text) + 10).coerceAtLeast(50))
            .position(x, y)
            .build()

    private fun addSkillScroll() {
        skillScroll = SkillContainerWidget(
            client!!, 0, 30,
            width / 2, getBottomY(), 40,
            { player.learnedSkills },
            ::renderSkillLine
        ).apply { onClick = ::handleSelectionButton }.also(::addDrawableChild)
    }

    private fun handleSelectionButton(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        line: SkillContainerWidget.SkillLine,
    ): Boolean {
        val skill = line.skill
        if (button == 1) {
            openEnhancementList(skill, mouseX, mouseY)
            return false
        }

        if (button != 0) return false
        val validSlot = getValidSlot(skill)
        val rightX = skillScroll.x + skillScroll.width
        if (validSlot != null && mouseX.toInt() in rightX - 22..rightX - 6) {
            player.equip(skill, validSlot)
            selectedSlot = null
        } else {
            if (System.currentTimeMillis() - line.lastClickTime < 250L) {
                client!!.setScreen(SkillGalleryScreen(skill, this))
            }
            if (selectedSkill != skill) {
                selectedSkill = skill
            } else {
                selectedSkill = null
                skillScroll.focused = null
            }
            updateScreen()
            if (selectedSkill == null) {
                line.lastClickTime = System.currentTimeMillis()
                return false
            }
        }
        return true
    }

    private fun renderSkillLine(
        context: DrawContext,
        index: Int,
        skill: Skill,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        focused: Boolean,
        tickDelta: Float,
    ) {
        val equipX = x + width - 22
        val right = x + width - 6
        val selected = selectedSkill == skill
        if (!hasOverlay() && (selected || focused || hovered)) {
            context.overlayHighlightWithSize(x, y, width, height, selected)
        }
        val gap = 5
        var currentX = x + gap * 2

        val iconY = y + (height - 16) / 2
        SkillRenderer.renderIcon(skill, context, currentX, iconY)
        if (!hasOverlay() && mouseX in currentX..currentX + 16 && mouseY in iconY..iconY + 16) {
            setTooltip(SkillRenderer.getTooltip(client!!, skill, player))
        }

        currentX += 16 + gap
        val name = skill.formattedName
        val titleY = y + height / 2 - textRenderer.fontHeight - 1
        context.drawText(textRenderer, name, currentX, titleY, 0xFFFFFF, false)
        context.drawText(
            textRenderer,
            "(".toText().append(skill.cooldownText).append(")"),
            currentX + textRenderer.getWidth(name) + gap,
            titleY,
            0xBDBDBD,
            false
        )
        val hasValidSlot = getValidSlot(skill) != null
        renderDescription(context, currentX, y + height / 2 + 1, equipX, skill.description, hasValidSlot)
        if (hasValidSlot) {
            if (!hasOverlay() && mouseX in equipX..<right && mouseY in y..<y + height) {
                context.overlayHighlight(equipX, y, right, y + height, false)
            }
            context.drawTexture(equipTexture, equipX, iconY, 0f, 0f, 16, 16, 16, 16)
        }
    }

    private fun renderDescription(
        context: DrawContext,
        left: Int,
        top: Int,
        right: Int,
        description: Text,
        hasValidSlot: Boolean,
    ) {
        val maxWidth = (if (hasValidSlot) right - 5 else right + 8) - left
        val text = if (textRenderer.getWidth(description) > maxWidth)
            textRenderer.trimToWidth(description, maxWidth).string.run {
                if (length > 2 && get(length - 2) == ' ')
                    substring(0, length - 2) + "..."
                else substring(0, length - 1) + "..."
            } else description.string
        context.drawText(textRenderer, text, left, top, 0xFFFFFF, false)
    }

    private fun addSkillSlots() {
        val totalColumns = (container.slotSize / 2 + if (container.slotSize % 2 == 0) 0 else 1).coerceAtLeast(1)
        val totalRows = if (container.slotSize > 1) 2 else 1
        val centerX = width / 2
        val bottomY = getBottomY()
        val slotWidth = (centerX * 0.47).toInt()
        val slotHeight = (bottomY * 0.17).toInt()
        val horizontalSpacing = (centerX - totalRows * slotWidth) / (totalRows + 1)
        val verticalSpacing = (bottomY - totalColumns * slotHeight) / (totalColumns)

        container.getAllSlots().forEach { slot ->
            val columnIndex = (slot.index - 1) % 2
            val rowIndex = (slot.index - 1) / 2
            val slotX = centerX + horizontalSpacing + columnIndex * (slotWidth + horizontalSpacing)
            val slotY = 30 + verticalSpacing / 2 + rowIndex * (slotHeight + verticalSpacing)
            val equippedSkillSlot = EquippedSkillSlot(
                slot.index, slotX, slotY,
                slotWidth, slotHeight
            )
            addDrawableChild(equippedSkillSlot)
            skillSlots.add(equippedSkillSlot)
        }
    }

    private fun getBottomY() = height - if (showCreativeButtons) 60 else 35

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)

        val level = player.skillLevel
        val cycle = player.skillCycle
        context.drawText(
            textRenderer,
            translate(
                "screen.list.level",
                "$level${if (cycle > 0) " (+$cycle)" else ""}"
            ),
            5, 5, 0xFFFFFF, false
        )
        context.drawText(
            textRenderer,
            translate("screen.list.exp", player.skillExp, PlayerUtils.getNextLevelExp(level)),
            5, 5 + textRenderer.fontHeight + 3, 0xFFFFFF, false
        )
        if (skillScroll.children().isEmpty()) {
            val emptyText = translate("screen.list.empty")
            context.drawText(
                textRenderer,
                emptyText,
                width / 4 - textRenderer.getWidth(emptyText) / 2,
                height / 2,
                0xFFFFFF,
                false
            )
        }
        if (!learnButton.active) {
            val text = if (player.choiceData.isCompleted()) {
                translate("screen.list.enhancedAll")
            } else {
                val requiredLevels = PlayerUtils.getLevelRequiredForLearningSkill(level)
                translate("screen.list.requiredLevel.learn", requiredLevels)
            }
            context.drawText(
                textRenderer,
                text,
                learnButton.x - 5 - textRenderer.getWidth(text),
                learnButton.y + (learnButton.height - textRenderer.fontHeight) / 2 + 1,
                11184810,
                false
            )
        }

        enhancementList.render(context, mouseX, mouseY, delta)
    }

    override fun update() = updateScreen()

    fun updateScreen() {
        if (skillScroll.children().size != player.learnedSkills.size) {
            skillScroll.refresh()
        }
        learnButton.active = !player.choiceData.isEmpty()
        if (player.hasLearnedAll()) {
            learnButton.message = translate("screen.list.button.enhance")
        } else {
            learnButton.message = translate("screen.list.button.learn")
        }

        enhancementList.update()
    }

    override fun shouldPause(): Boolean = false

    private fun getValidSlot(skill: Skill): Int? {
        val emptyIndex = container.getEmptySlot(skill)?.index
        val selectedSkill = selectedSlotSkill
        return when {
            selectedSkill != null && selectedSkill != skill && player.getSlot(selectedSlot!!)
                ?.canEquip(skill) == true -> selectedSlot

            emptyIndex != null && container.getSlot(skill) == null -> emptyIndex
            else -> null
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (enhancementList.keyPressed(keyCode, scanCode, modifiers)) {
            return true
        }
        if (client!!.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            close()
            return true
        }
        return !hasOverlay() && super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (hasOverlay()) {
            enhancementList.mouseMoved(mouseX, mouseY)
        } else {
            super.mouseMoved(mouseX, mouseY)
        }
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (enhancementList.keyReleased(keyCode, scanCode, modifiers)) {
            return true
        }
        return !hasOverlay() && super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (enhancementList.mouseClicked(mouseX, mouseY, button)) {
            return true
        }
        if (hasOverlay() && !enhancementList.isMouseOver(mouseX, mouseY)) {
            enhancementList.close()
            return true
        }
        return !hasOverlay() && super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (enhancementList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true
        }
        return !hasOverlay() && super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (enhancementList.mouseReleased(mouseX, mouseY, button)) {
            return true
        }
        return !hasOverlay() && super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if (enhancementList.mouseScrolled(mouseX, mouseY, amount)) {
            return true
        }
        return !hasOverlay() && super.mouseScrolled(mouseX, mouseY, amount)
    }

    private fun hasOverlay() = enhancementList.visible

    private fun openEnhancementList(skill: Skill, mouseX: Double, mouseY: Double) {
        if (skill.isEmpty) return

        enhancementList.apply {
            var x = mouseX.toInt() + 12
            val y = mouseY.toInt() - 12
            if (x + width > this@SkillListScreen.width) {
                x = mouseX.toInt() - width - 12
            }
            setPosition(x, y)
            fixOverflow(this@SkillListScreen.width, this@SkillListScreen.height)
            updateSkill(skill)
            visible = true
        }
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val old = enhancementList
        super.resize(client, width, height)
        enhancementList.restoreFrom(old, width to height)
    }

    inner class EquippedSkillSlot(
        private val slot: Int, x: Int, y: Int, width: Int, height: Int,
    ) : ClickableWidget(x, y, width, height, Text.empty()) {

        var skill: Skill
            get() = player.getSkill(slot)
            set(value) {
                player.equip(value, slot)
            }
        private val skillSlot: SkillSlot?
            get() = player.getSlot(slot)
        private var lastClickTime: Long = 0

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true
            } else if (button == 1 && clicked(mouseX, mouseY)) {
                openEnhancementList(skill, mouseX, mouseY)
                return true
            }
            return false
        }

        override fun onClick(mouseX: Double, mouseY: Double) {
            if (!skill.disabled && System.currentTimeMillis() - lastClickTime < 250L) {
                player.equip(Skills.EMPTY, slot)
            }
            lastClickTime = System.currentTimeMillis()
            selectedSlot = if (selectedSlot != slot) slot else null
            updateScreen()
        }

        override fun playDownSound(soundManager: SoundManager) = Unit

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.renderTooltip(x, y, width, height)
            val selected = selectedSlot == slot
            if (!hasOverlay() && (selected || hovered)) {
                context.fill(
                    x + 3,
                    y + 3,
                    x + width - 3,
                    y + height - 3,
                    Color.WHITE.alpha(if (selected) 0.4 else 0.2).rgb
                )
            }
            val skill = skill
            if (!skill.disabled) {
                val iconX = x + 8
                val iconY = y + (height - 16) / 2
                SkillRenderer.renderIcon(skill, context, iconX, iconY)
                if (!hasOverlay() && mouseX in iconX..iconX + 16 && mouseY in iconY..iconY + 16) {
                    setTooltip(SkillRenderer.getTooltip(client!!, skill, player))
                }

                val topY = y + (height - textRenderer.fontHeight) / 2 + 1
                context.drawScrollableText(
                    textRenderer,
                    skill.formattedName,
                    iconX + 24 + 3,
                    topY,
                    iconX + width - 16,
                    topY + textRenderer.fontHeight,
                    0xFFFFFF,
                    shadow = false,
                    center = false
                )
            }
            val slot = skillSlot
            context.drawTexture(
                indexTexture,
                x + width - 13,
                y + 4,
                slot?.u ?: 0,
                slot?.v ?: 0,
                9,
                9
            )
            if (!hasOverlay() && slot != null && mouseX in x + width - 13..x + width - 13 + 9 && mouseY in y + 4..y + 4 + 9) {
                setTooltip(slot.tooltip)
            }
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) = Unit
    }

    companion object {

        private val equipTexture = id("equip.png")
    }
}