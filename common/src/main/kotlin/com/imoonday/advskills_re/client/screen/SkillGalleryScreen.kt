package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.client.screen.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.tooltip.*
import net.minecraft.util.*

private const val issueUrl = "https://github.com/iMoonDay/AdvancedSkills/issues"

class SkillGalleryScreen(
    private var positioning: Skill? = null,
    private val parent: Screen? = null,
) : Screen(translate("screen.gallery.title")) {

    var selectedSkill: Skill? = null
    private lateinit var skillScroll: SkillContainerWidget
    private lateinit var infoBox: InfoBox

    override fun init() {
        super.init()
        skillScroll = SkillContainerWidget(
            client!!,
            0,
            20,
            (width * 0.55).toInt(),
            height - 30,
            34,
            { Skills.getValidSkills() }
        ) { context, index, skill, x, y, width, height, _, _, hovered, focused, _ ->
            val selected = selectedSkill == skill
            if (selected || focused || hovered) {
                context.overlayHighlightWithSize(x, y, width, height, selected)
            }
            val gap = 5
            var currentX = x + gap * 2
            val indexText = index.toString().padStart(3, '0')
            context.drawText(
                textRenderer,
                indexText,
                currentX,
                y + (height - textRenderer.fontHeight) / 2 + 1,
                0xFFFFFF,
                false
            )

            currentX += textRenderer.getWidth(indexText) + gap
            SkillRenderer.renderIcon(skill, context, currentX, y + (height - 16) / 2)

            currentX += 16 + gap
            val name = skill.formattedName
            context.drawText(
                textRenderer,
                name,
                currentX,
                y + height / 2 - textRenderer.fontHeight - 1,
                0xFFFFFF,
                false
            )
            context.drawText(
                textRenderer,
                skill.rarity.displayName.copy()
                    .append(" | ")
                    .append(skill.types.joinToString(" ") { it.displayName.string }),
                currentX,
                y + height / 2 + 1,
                0xFFFFFF,
                false
            )
        }.apply {
            onClick = { _, _, button, line ->
                if (button == 0) {
                    selectedSkill = line.skill
                    true
                } else false
            }
        }.also(::addDrawableChild)
        infoBox =
            InfoBox(width = (width * 0.45).toInt() - 10, x = (width * 0.55).toInt() + 5, y = 20).also(::addDrawable)
        ButtonIconWidget(width - 20, height - 20, 16, 16, suggestionTexture)
            .addClickAction(0) { Util.getOperatingSystem().open(issueUrl) }
            .apply {
                tooltip = Tooltip.of(
                    translate("screen.gallery.suggestion")
                        .append("\n")
                        .append(issueUrl.toText().formatted(Formatting.GRAY))
                )
            }.also(::addDrawableChild)

        positioning?.let {
            skillScroll.moveTo(it)
            selectedSkill = it
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() = client!!.setScreen(parent)

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val scrollAmount = skillScroll.scrollAmount
        positioning = selectedSkill
        super.resize(client, width, height)
        skillScroll.scrollAmount = scrollAmount
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 1) {
            close()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    inner class InfoBox(
        var width: Int = 0,
        var x: Int = 0,
        var y: Int = 0,
    ) : Drawable {

        private var height = 0

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            selectedSkill?.let {
                context.renderTooltip(x, y, width, height)
                height = renderInfo(context, it) - 4
            }
        }

        fun renderInfo(context: DrawContext, skill: Skill): Int {
            val gap = 5
            val xOffset = x + 8
            var yOffset = y + gap

            SkillRenderer.renderIcon(skill, context, x + width / 2 - 16, yOffset, 32)
            yOffset += 32 + gap

            val nameText = skill.formattedName
            context.drawText(
                textRenderer,
                nameText,
                x + width / 2 - textRenderer.getWidth(nameText) / 2,
                yOffset,
                0xFFFFFF,
                false
            )
            yOffset += textRenderer.fontHeight + gap

            yOffset += gap

            val description = skill.description
            textRenderer.wrapLines(description, width - 15).forEach {
                context.drawText(
                    textRenderer,
                    it,
                    xOffset,
                    yOffset,
                    0xBDBDBD,
                    false
                )
                yOffset += textRenderer.fontHeight + gap
            }

            yOffset += gap

            context.drawText(
                textRenderer,
                translate("screen.gallery.info.type", skill.types.joinToString(" ") { it.displayName.string }),
                xOffset,
                yOffset,
                0x4FC3F7,
                false
            )
            yOffset += textRenderer.fontHeight + gap

            context.drawText(
                textRenderer,
                translate("screen.gallery.info.cooldown", skill.cooldownText),
                xOffset,
                yOffset,
                0x81C784,
                false
            )
            yOffset += textRenderer.fontHeight + gap


            context.drawText(
                textRenderer,
                translate("screen.gallery.info.rarity", skill.rarity.displayName).formatted(skill.rarity.formatting),
                xOffset,
                yOffset,
                0xFFFFFF,
                false
            )
            return yOffset
        }
    }

    companion object {

        private val suggestionTexture = id("suggestion.png")
    }
}