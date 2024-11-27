package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.narration.*
import net.minecraft.client.gui.widget.*
import net.minecraft.entity.player.*
import net.minecraft.text.*

class SkillLearningScreen(
    val player: PlayerEntity,
    val parent: () -> Screen? = { null },
) : Screen(Text.empty()), AutoSyncedScreen {

    private val choice: SkillChoice
        get() = player.getChoice()
    private val skillBoxes: MutableList<SkillBox> = mutableListOf()
    private var selectedBox: SkillBox? = null
    private lateinit var refreshButton: ButtonWidget
    private lateinit var learnButton: ButtonWidget

    override fun init() {
        super.init()
        val boxWidth = 120
        val boxHeight = (height * 0.65).toInt()
        val spacing = ((width - 3 * boxWidth) / 5).coerceAtLeast(5)
        val totalWidth = boxWidth * 3 + spacing * 2
        val startX = (width - totalWidth) / 2
        SkillBox({ choice.first }, player::chooseFirst, startX, 40, boxWidth, boxHeight)
            .also {
                skillBoxes += it
                addDrawableChild(it)
            }
        SkillBox({ choice.second }, player::chooseSecond, startX + boxWidth + spacing, 40, boxWidth, boxHeight)
            .also {
                skillBoxes += it
                addDrawableChild(it)
            }
        SkillBox({ choice.third }, player::chooseThird, startX + (boxWidth + spacing) * 2, 40, boxWidth, boxHeight)
            .also {
                skillBoxes += it
                addDrawableChild(it)
            }
        val buttonY = (40 + boxHeight + height) / 2 - 10
        refreshButton = ButtonWidget.builder(translate("screen.learn.refresh")) { player.refreshChoice() }
            .dimensions(width / 3 - 25, buttonY, 50, 20)
            .build()
            .also(::addDrawableChild)
        learnButton = ButtonWidget.builder(translate("screen.learn.learn")) { selectedBox?.choose() }
            .dimensions(width / 3 * 2 - 25, buttonY, 50, 20)
            .build()
            .also(::addDrawableChild)

        new = false
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val countText = translate("screen.learn.count", player.learnableData.count)
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
        if (player.learnableData.isEmpty()) close()
        else skillBoxes.forEach(SkillBox::updateSkill)
        updateButtons()
    }

    private fun updateButtons() {
        refreshButton.active = player.canFreshChoice()
        learnButton.active = selectedBox != null && !selectedBox!!.skill.invalid
    }

    override fun close() = client!!.setScreen(parent())

    inner class SkillBox(
        private val skillGetter: () -> Skill, private val chooseAction: () -> Unit,
        x: Int, y: Int, width: Int, height: Int,
    ) : ClickableWidget(x, y, width, height, skillGetter().name) {

        var skill: Skill = skillGetter()
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
            if (skill.invalid) return
            val gap = 5
            val x = x + 8
            var y = y + gap

            skill.renderIcon(context, this.x + (width - 32) / 2, y, 32)
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

            context.drawScrollableText(
                textRenderer,
                skill.cooldownSeconds,
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0x81C784, false
            )
            y += textRenderer.fontHeight + 2

            context.drawScrollableText(
                textRenderer,
                Text.literal(skill.types.joinToString(", ") { type -> type.displayName.string }),
                x, y,
                x + width - 15, y + textRenderer.fontHeight,
                0x4FC3F7, false
            )
            y += textRenderer.fontHeight + 5

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

        fun choose() {
            chooseAction()
            if (player.learnableData.hasNext()) updateSkill() else close()
        }

        fun updateSkill() {
            skill = skillGetter()
            selectedBox = null
            updateButtons()
        }
    }

    companion object {

        var new = false
    }
}