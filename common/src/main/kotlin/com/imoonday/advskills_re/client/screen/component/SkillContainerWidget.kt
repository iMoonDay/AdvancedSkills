package com.imoonday.advskills_re.client.screen.component

import com.imoonday.advskills_re.client.screen.component.SkillContainerWidget.*
import com.imoonday.advskills_re.skill.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.widget.*
import net.minecraft.text.*

class SkillContainerWidget(
    client: MinecraftClient,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    itemHeight: Int,
    private val skillGetter: () -> Collection<Skill>,
    private val skillRenderer: ISkillRenderer,
) : AlwaysSelectedEntryListWidget<SkillLine>(
    client, width, height, y, y + height, itemHeight,
) {

    var x: Int
        get() = super.left
        set(value) {
            super.setLeftPos(value)
        }
    var y: Int
        get() = super.top
        set(value) {
            super.top = value
            super.bottom = value + super.height
        }
    var width: Int
        get() = super.width
        set(value) {
            super.width = value
        }
    var height: Int
        get() = super.height
        set(value) {
            super.height = value
            super.bottom = y + value
        }
    var onClick: ((mouseX: Double, mouseY: Double, button: Int, line: SkillLine) -> Boolean)? =
        { _, _, button, _ -> button == 0 }

    init {
        this.setLeftPos(x)
        this.setRenderBackground(false)
        this.setRenderSelection(false)
        this.setRenderHeader(false, 0)
        this.setRenderHorizontalShadows(false)
        this.addSkillEntries()
    }

    fun refresh() {
        this.clearEntries()
        this.addSkillEntries()
    }

    private fun addSkillEntries() {
        skillGetter().forEachIndexed { index, skill -> this.addEntry(SkillLine(index + 1, skill)) }
    }

    fun moveTo(skill: Skill) {
        this.children().find { it is SkillLine && it.skill == skill }?.let {
            this.ensureVisible(it)
            this.focusOn(it)
        }
    }

    override fun getScrollbarPositionX(): Int = super.width - 6

    override fun getRowWidth(): Int = super.width

    override fun getRowLeft(): Int = left

    inner class SkillLine(val index: Int, val skill: Skill) : Entry<SkillLine>() {

        var lastClickTime: Long = 0

        override fun render(
            context: DrawContext,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float,
        ) = skillRenderer.render(
            context,
            this.index,
            skill,
            x,
            y,
            entryWidth,
            entryHeight + 4,
            mouseX,
            mouseY,
            hovered,
            isFocused,
            tickDelta
        )

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
            onClick?.invoke(mouseX, mouseY, button, this)?.also { if (it) lastClickTime = System.currentTimeMillis() }
                ?: false

        override fun getNarration(): Text = skill.name
    }

    fun interface ISkillRenderer {

        fun render(
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
        )
    }
}