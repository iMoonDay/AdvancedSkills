package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.narration.*
import net.minecraft.client.gui.widget.*
import net.minecraft.entity.player.*
import net.minecraft.text.*

class SkillEnhancementScreen(
    val player: PlayerEntity,
    val parent: () -> Screen? = { null },
) : Screen(Text.empty()), Syncable {

    private val choice: Choice<EnhancementChoice.Pair>
        get() = player.getEnhancementChoice()
    private val skillEnhancementBoxes: MutableList<SkillEnhancementBox> = mutableListOf()
    private var selectedBox: SkillEnhancementBox? = null
    private lateinit var refreshButton: ButtonWidget
    private lateinit var enhanceButton: ButtonWidget

    override fun init() {
        super.init()
        val boxWidth = (width / 4).coerceAtMost(140)
        val boxHeight = (boxWidth * 1.5).coerceAtMost(height * 0.65).toInt()
        val spacing = ((width - 3 * boxWidth) / 5).coerceAtLeast(5)
        val totalWidth = boxWidth * 3 + spacing * 2
        val startX = (width - totalWidth) / 2
        SkillEnhancementBox({ choice.first }, player::chooseFirst, startX, 40, boxWidth, boxHeight)
            .also {
                skillEnhancementBoxes += it
                addDrawableChild(it)
            }
        SkillEnhancementBox(
            { choice.second },
            player::chooseSecond,
            startX + boxWidth + spacing,
            40,
            boxWidth,
            boxHeight
        )
            .also {
                skillEnhancementBoxes += it
                addDrawableChild(it)
            }
        SkillEnhancementBox(
            { choice.third },
            player::chooseThird,
            startX + (boxWidth + spacing) * 2,
            40,
            boxWidth,
            boxHeight
        )
            .also {
                skillEnhancementBoxes += it
                addDrawableChild(it)
            }
        val buttonY = (40 + boxHeight + height) / 2 - 10
        refreshButton =
            ButtonWidget.builder(translate("screen.learn.refresh")) { player.refreshSkillChoice(RefreshChoiceC2SRequest.Type.ENHANCEMENT) }
                .dimensions(width / 3 - 25, buttonY, 50, 20)
                .build()
                .also(::addDrawableChild)
        enhanceButton = ButtonWidget.builder(translate("screen.enhance.enhance")) { selectedBox?.choose() }
            .dimensions(width / 3 * 2 - 25, buttonY, 50, 20)
            .build()
            .apply { active = false }
            .also(::addDrawableChild)

        new = false
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val countText = translate("screen.enhance.count", player.enhancementData.count)
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
        if (player.enhancementData.isEmpty()) {
            close()
            return
        } else {
            skillEnhancementBoxes.forEach(SkillEnhancementBox::updateSkill)
        }
        updateButtons()
    }

    private fun updateButtons() {
        refreshButton.active = player.canFreshChoice(RefreshChoiceC2SRequest.Type.ENHANCEMENT)
        enhanceButton.active = selectedBox != null && !selectedBox!!.pair.isEmtpy()
    }

    override fun close() = client!!.setScreen(parent())

    inner class SkillEnhancementBox(
        private val pairGetter: () -> EnhancementChoice.Pair,
        private val chooseAction: () -> Unit,
        x: Int, y: Int, width: Int, height: Int,
    ) : ClickableWidget(x, y, width, height, pairGetter().skill.name) {

        var pair: EnhancementChoice.Pair = pairGetter()
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
            if (pair.isEmtpy()) return
            val gap = 5
            val x = x + 8
            var y = y + gap

            val skill = pair.skill
            val enhancement = pair.enhancement

            val levelText = enhancement.level.toString().toText()
            context.drawText(
                textRenderer,
                levelText,
                x + width - 15 - textRenderer.getWidth(levelText),
                y + 2,
                skill.rarity.color,
                false
            )

            SkillRenderer.renderIcon(skill, context, this.x + (width - 32) / 2, y, 32)
            y += 32 + 3
            val textBottomY = this.y + height - gap
            context.enableScissor(x, y, x + width - 15, textBottomY)
            y -= scrollAmount
            context.drawScrollableText(
                textRenderer,
                skill.formattedName,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0xFFFFFF, false
            )
            y += textRenderer.fontHeight + 2

            y += textRenderer.fontHeight + 2
            context.drawScrollableText(
                textRenderer,
                enhancement.name,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0x81C784, false
            )
            y += textRenderer.fontHeight + 2

            textRenderer.wrapLines(skill.getEnhancementTooltip(enhancement), width - 15).forEach { text ->
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

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) = Unit

        override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
            val max = maxScrollAmount ?: 0
            if (max > 0) {
                if (amount > 0) {
                    scrollAmount = (scrollAmount - 5).coerceAtLeast(0)
                } else if (amount < 0) {
                    scrollAmount = (scrollAmount + 5).coerceAtMost(max)
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
            val new = pairGetter()
            if (selectedBox == this && new != pair) {
                selectedBox = null
            }
            pair = new
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