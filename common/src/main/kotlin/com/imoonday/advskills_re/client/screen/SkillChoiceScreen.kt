package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.choice.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.narration.*
import net.minecraft.client.gui.widget.*
import net.minecraft.entity.player.*
import net.minecraft.text.*
import net.minecraft.util.*

class SkillChoiceScreen(
    val player: PlayerEntity,
    val parent: () -> Screen? = { null },
) : Screen(Text.empty()), Syncable {

    private val choice: Choice
        get() = player.getChoice()
    private val choiceBoxes: MutableList<ChoiceBox> = mutableListOf()
    private var selectedBox: ChoiceBox? = null
    private lateinit var refreshButton: ButtonWidget
    private lateinit var chooseButton: ButtonWidget

    override fun init() {
        super.init()
        val boxWidth = (width / 4).coerceAtMost(140)
        val boxHeight = (boxWidth * 1.5).coerceAtMost(height * 0.65).toInt()
        val spacing = ((width - 3 * boxWidth) / 5).coerceAtLeast(5)
        val totalWidth = boxWidth * 3 + spacing * 2
        val startX = (width - totalWidth) / 2
        ChoiceBox({ choice.first }, player::chooseFirst, startX, 40, boxWidth, boxHeight)
            .also {
                choiceBoxes += it
                addDrawableChild(it)
            }
        ChoiceBox({ choice.second }, player::chooseSecond, startX + boxWidth + spacing, 40, boxWidth, boxHeight)
            .also {
                choiceBoxes += it
                addDrawableChild(it)
            }
        ChoiceBox({ choice.third }, player::chooseThird, startX + (boxWidth + spacing) * 2, 40, boxWidth, boxHeight)
            .also {
                choiceBoxes += it
                addDrawableChild(it)
            }
        val buttonY = (40 + boxHeight + height) / 2 - 10
        val text = createRefreshButtonText()
        refreshButton =
            ButtonWidget.builder(text) { player.refreshSkillChoice() }
                .dimensions(width / 3 - 25, buttonY, 50, 20)
                .build()
                .apply { active = player.canFreshChoice() }
                .also(::addDrawableChild)
        chooseButton = ButtonWidget.builder(translate("screen.choice.choose")) { selectedBox?.choose() }
            .dimensions(width / 3 * 2 - 25, buttonY, 50, 20)
            .build()
            .apply { active = false }
            .also(::addDrawableChild)

        new = false
    }

    private fun createRefreshButtonText(): MutableText {
        var text = translate("screen.choice.refresh")
        val refreshableCount = player.choiceData.refreshableCount
        if (refreshableCount > 0) {
            text = text.append(" ($refreshableCount)")
        }
        return text
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val countText = translate("screen.choice.count", player.choiceData.count)
        context.drawText(
            textRenderer,
            countText,
            width / 2 - textRenderer.getWidth(countText) / 2,
            (40 - textRenderer.fontHeight) / 2,
            0xFFFFFF,
            false
        )
    }

    override fun update() {
        if (player.choiceData.isEmpty()) {
            close()
            return
        } else {
            choiceBoxes.forEach(ChoiceBox::updateSkill)
        }
        updateButtons()
    }

    private fun updateButtons() {
        refreshButton.active = player.canFreshChoice()
        refreshButton.message = createRefreshButtonText()
        chooseButton.active = selectedBox != null && !selectedBox!!.choice.isEmpty()
    }

    override fun close() = client!!.setScreen(parent())

    inner class ChoiceBox(
        private val choiceGetter: () -> Choosable,
        private val chooseAction: () -> Unit,
        x: Int, y: Int, width: Int, height: Int,
    ) : ClickableWidget(x, y, width, height, choiceGetter().skill.name) {

        var choice = choiceGetter()
        private var lastClickTime = 0L
        var scrollAmount: Int = 0
        var maxScrollAmount: Int? = null

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            val selected = selectedBox == this
            if (selected || hovered) {
                val light = if (selected) 0.45f else 0.35f
                context.setShaderColor(light, light, light, 1f)
                context.renderPanel(x, y, width, height)
                context.setShaderColor(1f, 1f, 1f, 1f)
            } else {
                context.renderDarkPanel(x, y, width, height)
            }

            when (val choosable = choice) {
                is SkillChoice -> {
                    renderSkillBox(context, choosable)
                }

                is EnhancementChoice -> {
                    renderEnhancementBox(context, mouseX, mouseY, choosable)
                }
            }
        }

        private fun renderSkillBox(context: DrawContext, choice: SkillChoice) {
            val skill = choice.skill

            if (skill.invalid) return
            val gap = 5
            val x = x + 8
            var y = y + gap

            val rarity = skill.rarity.roman
            context.drawText(
                textRenderer,
                rarity,
                x + width - 15 - textRenderer.getWidth(rarity),
                y + 2,
                skill.rarity.color,
                false
            )

            SkillRenderer.renderIcon(skill, context, this.x + (width - 32) / 2, y, 32)
            y += 32 + 3
            val textBottomY = this.y + height - gap

            context.drawScrollableText(
                textRenderer,
                skill.formattedName,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0xFFFFFF, false
            )
            y += textRenderer.fontHeight + 2

            context.drawScrollableText(
                textRenderer,
                skill.cooldownText,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0x81C784, false
            )
            y += textRenderer.fontHeight + 2

            context.drawScrollableText(
                textRenderer,
                skill.types.joinToString(", ") { type -> type.displayName.string }.toText(),
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0x4FC3F7, false
            )
            y += textRenderer.fontHeight + 5

            context.enableScissor(x, y, x + width - 15, textBottomY)
            y -= scrollAmount

            textRenderer.wrapLines(skill.description, width - 15).forEach { text ->
                context.drawText(
                    textRenderer,
                    text,
                    (x + x + width - 15 - textRenderer.getWidth(text)) / 2,
                    y,
                    0xBDBDBD,
                    false
                )
                y += textRenderer.fontHeight + 3
            }
            y -= 2

            context.disableScissor()

            if (maxScrollAmount == null) {
                maxScrollAmount = if (y - textBottomY > 0 && y - 3 - textBottomY <= 0) 0
                else (y - textBottomY).coerceAtLeast(0)
            }
        }

        fun renderEnhancementBox(context: DrawContext, mouseX: Int, mouseY: Int, choice: EnhancementChoice) {
            val skill = choice.skill
            val enhancement = choice.enhancement ?: return
            val currentLevel = player.getCurrentEnhancementLvl(skill, choice.enhancementId)

            if (choice.isEmpty()) return
            val gap = 5
            val x = x + 8
            var y = y + gap

            val iconX = this.x + (width - 32) / 2
            SkillRenderer.renderIcon(skill, context, iconX, y, 32)
            if (mouseX in iconX..iconX + 32 && mouseY in y..y + 32) {
                setTooltip(SkillRenderer.getTooltip(client!!, skill, player))
            }

            if (currentLevel <= 0) {
                val new = translate("screen.choice.new")
                context.drawText(
                    textRenderer,
                    new,
                    this.x + width - textRenderer.getWidth(new) - 5,
                    y + 1,
                    0xFFFFFF,
                    false
                )
            }

            y += 32 + 3
            val textBottomY = this.y + height - gap

            context.drawScrollableText(
                textRenderer,
                skill.formattedName,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0xFFFFFF, false
            )
            y += textRenderer.fontHeight + 2

            y += (textRenderer.fontHeight + 2) / 2
            context.drawScrollableText(
                textRenderer,
                enhancement.name,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0x81C784, false
            )
            y += textRenderer.fontHeight + 2

            y += (textRenderer.fontHeight + 2) / 2

            context.enableScissor(x, y, x + width - 15, textBottomY)
            y -= scrollAmount

            currentLevel.takeIf { it > 0 }?.run {
                val tooltip = skill.getEnhancementTooltip(enhancement.id, this)
                textRenderer.wrapLines(tooltip, width - 15).forEach { text ->
                    context.drawText(
                        textRenderer,
                        text,
                        (x + x + width - 15 - textRenderer.getWidth(text)) / 2,
                        y,
                        0xBDBDBD,
                        false
                    )
                    y += textRenderer.fontHeight + 3
                }

                val arrow = "↓"
                context.drawText(
                    textRenderer,
                    arrow,
                    (x + x + width - 15 - textRenderer.getWidth(arrow)) / 2,
                    y,
                    0xFFFFFF,
                    false
                )
                y += textRenderer.fontHeight + 3
            }

            val tooltip = skill.getEnhancementTooltip(enhancement.id, currentLevel + 1)
            textRenderer.wrapLines(tooltip, width - 15).forEach { text ->
                context.drawText(
                    textRenderer,
                    text,
                    (x + x + width - 15 - textRenderer.getWidth(text)) / 2,
                    y,
                    0x00FF00,
                    false
                )
                y += textRenderer.fontHeight + 3
            }

            context.disableScissor()

            currentLevel.takeIf { it > 0 }?.run {
                val levelText = currentLevel.toString().toText().formatted(Formatting.GRAY)
                    .append(" → ".toText().formatted(Formatting.WHITE))
                    .append((currentLevel + 1).toString().toText().formatted(Formatting.GREEN))
                context.drawText(
                    textRenderer,
                    levelText,
                    (x + x + width - 15 - textRenderer.getWidth(levelText)) / 2,
                    this@ChoiceBox.y + height - textRenderer.fontHeight - 5,
                    0xFFFFFF,
                    false
                )
            }

            if (maxScrollAmount == null) {
                maxScrollAmount = if (y - textBottomY > 0 && y - 3 - textBottomY <= 0) 0
                else (y - textBottomY).coerceAtLeast(0)
            }
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) = Unit

        override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
            val max = maxScrollAmount ?: 0
            if (max > 0) {
                val step = max / 5
                if (amount > 0) {
                    scrollAmount = (scrollAmount - step).coerceAtLeast(0)
                } else if (amount < 0) {
                    scrollAmount = (scrollAmount + step).coerceAtMost(max)
                }
            }
            return super.mouseScrolled(mouseX, mouseY, amount)
        }

        override fun onClick(mouseX: Double, mouseY: Double) {
            super.onClick(mouseX, mouseY)
            selectedBox = if (selectedBox == this) null else this
            updateButtons()
            if (System.currentTimeMillis() - lastClickTime < 250L) {
                choose()
            }
            lastClickTime = System.currentTimeMillis()
        }

        fun choose() = chooseAction()

        fun updateSkill() {
            val newSkill = choiceGetter()
            if (selectedBox == this && newSkill != choice) {
                selectedBox = null
            }
            choice = newSkill
            updateButtons()
            lastClickTime = 0
            scrollAmount = 0
            maxScrollAmount = null
        }
    }

    companion object {

        var new = false
    }
}