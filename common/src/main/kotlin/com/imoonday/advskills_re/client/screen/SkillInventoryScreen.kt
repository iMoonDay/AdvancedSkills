package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.client.screen.component.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.SkillSlot.Companion.indexTexture
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.narration.*
import net.minecraft.client.gui.tooltip.*
import net.minecraft.client.gui.widget.*
import net.minecraft.entity.player.*
import net.minecraft.text.*
import net.minecraft.util.*
import org.lwjgl.glfw.*
import java.awt.*
import java.util.*

class SkillInventoryScreen(
    val player: PlayerEntity,
    val parent: () -> Screen? = { null },
) : Screen(Text.empty()), Syncable {

    var selectedSlot: Slot? = null
    var selectedTab: Tab? = null
        set(value) {
            field = value
            update()
        }
    var selectingSlot: Slot? = null
    private val tabs: MutableList<Tab> = mutableListOf()
    private val equippedSlots: MutableList<EquippedSlot> = mutableListOf()
    private var skillFilter: SkillFilter = SkillFilter.NONE
    private var rarityFilter: RarityFilter = RarityFilter.NONE
    private lateinit var slotGrid: SlotGrid
    private val displaySkills
        get() = player.learnedSkills
            .asSequence()
            .filterNot(player::hasEquipped)
            .filter { (selectedTab?.type ?: return@filter true) in it.types }
            .filter { skillFilter(player, it) && rarityFilter(it) }
            .sortedWith(ClientConfig.get().skillSorter)
            .toList()
    var bgWidth: Int = width
    var bgHeight: Int = height
    val bgX: Int
        get() = (width - bgWidth) / 2
    val bgY: Int
        get() = (height - bgHeight) / 2
    private var equippedSlotOffset: Int = 0
        set(value) {
            field = value.coerceIn(0, equippedSlots.size - 6)
        }

    override fun update() {
        slotGrid.replaceSlots(displaySkills)
        player.equippedSkills.forEachIndexed { index, skill ->
            equippedSlots[index].skill = skill
        }
    }

    override fun init() {
        super.init()
        bgWidth = 9 * (SLOT_SIZE + 4) + 15 + 13
        bgHeight = 6 * (SLOT_SIZE + 4)
        val halfSize = SkillType.entries.size / 2
        val gap = (bgWidth - 5 * 26) / 10
        tabs.clear()
        SkillType.entries.forEachIndexed { index, type ->
            val reverse = index >= halfSize
            val (x, y) = if (!reverse) {
                bgX + (26 + gap * 2) * index + gap to bgY - 28
            } else {
                bgX + (26 + gap * 2) * (index - halfSize) + gap to bgY + bgHeight - 4
            }
            Tab(type, index, x, y, reverse).also {
                addDrawableChild(it)
                tabs.add(it)
            }
        }
        slotGrid = SlotGrid(bgX, bgY + 15, 4, 9, 5, Insets(5, 8, 5, 15))
            .apply { replaceSlots(displaySkills) }
            .also(::addDrawableChild)
        equippedSlots.clear()
        player.skillContainer.getAllSlots().forEachIndexed { index, slot ->
            EquippedSlot(
                slot, bgX + 25 + index * (SLOT_SIZE + 14 + 5) - equippedSlotOffset * 43, bgY + bgHeight - 8 - SLOT_SIZE
            ).apply { visible = index in equippedSlotOffset..<equippedSlotOffset + 6 }
                .also {
                    addDrawableChild(it)
                    equippedSlots.add(it)
                }
        }
        val config = ClientConfig.get()
        var y = bgY + 5
        ExpandableIconButtonWidget(bgX - 16, y, 16, 16, sortTexture)
            .addClickAction(0) { updateSorter(config, it, true) }
            .addClickAction(1) { updateSorter(config, it, false) }
            .setScrollAction { widget, amount ->
                if (amount > 0) {
                    updateSorter(config, widget, false)
                    true
                } else if (amount < 0) {
                    updateSorter(config, widget, true)
                    true
                } else null
            }.apply {
                tooltip = createSorterTooltip()
                message = config.skillSorter.displayName
            }
            .also(::addDrawableChild)

        y += 16 + 5
        ExpandableIconButtonWidget(bgX - 16, y, 16, 16, filterTexture)
            .addClickAction(0) { updateFilter(it, true) }
            .addClickAction(1) { updateFilter(it, false) }
            .setScrollAction { widget, amount ->
                if (amount > 0) {
                    updateFilter(widget, false)
                    true
                } else if (amount < 0) {
                    updateFilter(widget, true)
                    true
                } else null
            }.apply {
                tooltip = createFilterTooltip()
                message = skillFilter.displayName
            }
            .also(::addDrawableChild)

        y += 16 + 5
        ExpandableIconButtonWidget(bgX - 16, y, 16, 16, rarityFilterTexture)
            .addClickAction(0) { updateRarityFilter(it, true) }
            .addClickAction(1) { updateRarityFilter(it, false) }
            .setScrollAction { widget, amount ->
                if (amount > 0) {
                    updateRarityFilter(widget, false)
                    true
                } else if (amount < 0) {
                    updateRarityFilter(widget, true)
                    true
                } else null
            }.apply {
                tooltip = createRarityFilterTooltip()
                message = rarityFilter.displayName
            }
            .also(::addDrawableChild)
    }

    private fun updateRarityFilter(
        widget: ButtonIconWidget,
        next: Boolean
    ) {
        rarityFilter = rarityFilter.run { if (next) next() else previous() }
        widget.tooltip = createRarityFilterTooltip()
        widget.message = rarityFilter.displayName
        update()
    }

    private fun createRarityFilterTooltip(): Tooltip {
        val rarity = rarityFilter
        return Tooltip.of(
            RarityFilter.entries.toText(
                formatter = {
                    it.displayName.copy()
                        .formatted(if (rarity == it) Formatting.GREEN else Formatting.GRAY)
                },
                separator = "\n".toText()
            )
        )
    }

    private fun updateFilter(
        widget: ButtonIconWidget,
        next: Boolean
    ) {
        skillFilter = skillFilter.run { if (next) next() else previous() }
        widget.tooltip = createFilterTooltip()
        widget.message = skillFilter.displayName
        update()
    }

    private fun createFilterTooltip(): Tooltip {
        val filter = skillFilter
        return Tooltip.of(
            SkillFilter.entries.toText(
                formatter = {
                    it.displayName.copy()
                        .formatted(if (filter == it) Formatting.GREEN else Formatting.GRAY)
                },
                separator = "\n".toText()
            )
        )
    }

    private fun updateSorter(
        config: ClientConfig,
        widget: ButtonIconWidget,
        next: Boolean
    ) {
        config.skillSorter = config.skillSorter.run { if (next) next() else previous() }
        widget.tooltip = createSorterTooltip()
        widget.message = config.skillSorter.displayName
        update()
    }

    private fun createSorterTooltip(): Tooltip {
        val sorter = ClientConfig.get().skillSorter
        return Tooltip.of(
            SkillSorter.entries.toText(
                formatter = {
                    it.displayName.copy()
                        .formatted(if (sorter == it) Formatting.GREEN else Formatting.GRAY)
                },
                separator = "\n".toText()
            )
        )
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val scrollAmount = slotGrid.scrollOffset
        super.resize(client, width, height)
        slotGrid.scrollOffset = scrollAmount
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val title = if (selectedTab == null) translate(
            "screen.inventory.title",
            player.learnedSkills.size,
            Skills.getValidSkills().size
        ) else selectedTab!!.type.displayName.copy().formatted(Formatting.BLACK)
        context.drawText(
            textRenderer,
            title,
            width / 2 - textRenderer.getWidth(title) / 2,
            bgY + 8,
            0x000000,
            false
        )
        selectedSlot?.run {
            context.drawTexture(skill.icon, mouseX - 8, mouseY - 8, 90, 0f, 0f, 16, 16, 16, 16)
        }
    }

    override fun renderBackground(context: DrawContext) {
        context.fillGradient(0, 0, this.width, this.height, -1, -1072689136, -804253680)
        context.renderPanel(bgX, bgY, bgWidth, bgHeight)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (client!!.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            close()
            return true
        }
        return if (selectingSlot != null && !selectingSlot!!.skill.invalid && selectingSlot != selectedSlot) {
            val slot = selectingSlot!!
            when (keyCode) {
                GLFW.GLFW_KEY_1 -> swap(slot, 1)
                GLFW.GLFW_KEY_2 -> swap(slot, 2)
                GLFW.GLFW_KEY_3 -> swap(slot, 3)
                GLFW.GLFW_KEY_4 -> swap(slot, 4)
                GLFW.GLFW_KEY_5 -> swap(slot, 5)
                GLFW.GLFW_KEY_6 -> swap(slot, 6)
                GLFW.GLFW_KEY_7 -> swap(slot, 7)
                GLFW.GLFW_KEY_8 -> swap(slot, 8)
                GLFW.GLFW_KEY_9 -> swap(slot, 9)
                GLFW.GLFW_KEY_0 -> swap(slot, 10)
                else -> super.keyPressed(keyCode, scanCode, modifiers)
            }
        } else super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun swap(slot: Slot, index: Int): Boolean {
        val original = player.getSkill(index)
        val result = player.equip(slot.skill, index)
        if (slot.slot != null && !original.invalid) {
            player.equip(original, slot.slot)
        }
        return result
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if (mouseX.toInt() in bgX..bgX + bgWidth && mouseY.toInt() in bgY + bgHeight - 8 - SLOT_SIZE..bgY + bgHeight - 8) {
            val slotWidth = 43
            if (amount > 0) {
                equippedSlotOffset--
            } else if (amount < 0) {
                equippedSlotOffset++
            }
            equippedSlots.forEachIndexed { index, slot ->
                slot.x = bgX + 25 + index * (SLOT_SIZE + 14 + 5) - equippedSlotOffset * slotWidth
                slot.visible = index in equippedSlotOffset..<equippedSlotOffset + 6
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun close() = client!!.setScreen(parent())

    override fun shouldPause(): Boolean = false

    open inner class Slot(
        val slot: SkillSlot?,
        var skill: Skill,
        var x: Int,
        var y: Int,
    ) : Drawable, Element, Selectable {

        var width: Int = SLOT_SIZE
        var height: Int = SLOT_SIZE
        var visible: Boolean = true
        var hovered: Boolean = false

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            if (!visible) return
            context.drawTexture(slotTexture, x, y, skill.rarity.level * 24f, 32f, width, height, 256, 256)
            context.fill(x + width - 3, y + 1, x + width - 1, y + 3, skill.rarity.color)
            if (!skill.invalid && selectedSlot?.skill != skill) {
                SkillRenderer.renderIcon(skill, context, x + 4, y + 4)
            }
            if (hovered) {
                val edge = 4
                context.overlayHighlight(x + edge, y + edge, x + width - edge, y + height - edge, true)
                selectingSlot = if (!skill.invalid) {
                    if (selectedSlot == null) {
                        if (!ClientConfig.get().hideSkillInfo || hasShiftDown()) {
                            setTooltip(SkillRenderer.getTooltip(client!!, skill, player))
                        } else {
                            setTooltip(skill.formattedName)
                        }
                    }
                    this
                } else {
                    null
                }
            } else if (selectingSlot == this) {
                selectingSlot = null
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (!visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false
            return onClick()
        }

        private fun onClick(): Boolean {
            if (slot == null) {
                if (hasShiftDown()) {
                    player.equip(skill)
                } else {
                    if (selectedSlot != null && selectedSlot!!.slot != null) {
                        player.equip(Skills.EMPTY, selectedSlot!!.slot!!)
                        selectedSlot = null
                    } else {
                        selectedSlot = if (selectedSlot == null && !skill.invalid) this else null
                    }
                }
            } else if (selectedSlot != null && selectedSlot != this) {
                if (!slot.canEquip(selectedSlot!!.skill)) return false
                player.equip(selectedSlot!!.skill, slot)
                if (selectedSlot!!.slot != null && !selectedSlot!!.skill.invalid) player.equip(
                    skill,
                    selectedSlot!!.slot!!
                )
                selectedSlot = null
            } else if (!skill.invalid) {
                if (hasShiftDown()) {
                    player.equip(Skills.EMPTY, slot)
                } else {
                    selectedSlot = if (selectedSlot != this) this else null
                }
            }
            return true
        }

        override fun setFocused(focused: Boolean) = Unit

        override fun isFocused(): Boolean = selectedSlot == this

        override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
            visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height

        override fun getNavigationFocus(): ScreenRect = ScreenRect(x, y, width, height)

        override fun appendNarrations(builder: NarrationMessageBuilder) = Unit

        override fun getType(): Selectable.SelectionType =
            if (this.isFocused) Selectable.SelectionType.FOCUSED
            else if (visible && selectingSlot == this) Selectable.SelectionType.HOVERED
            else Selectable.SelectionType.NONE
    }

    inner class EquippedSlot(slot: SkillSlot, x: Int, y: Int) : Slot(slot, slot.skill, x, y) {

        val indexX: Int
            get() = x - 14
        val indexY: Int
            get() = y + (height - 9) / 2
        val indexSize: Int = 9

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            if (!visible) return
            super.render(context, mouseX, mouseY, delta)
            selectingSlot?.let {
                if (hasShiftDown() && player.skillContainer.getEmptySlot(it.skill)?.index == slot?.index) {
                    context.drawBorder(x - 1, y - 1, width + 2, height + 2, 0xFF00FF00.toInt())
                }
            }
            slot?.let {
                context.drawTexture(
                    indexTexture, indexX, indexY, it.u.toFloat(), it.v.toFloat(), indexSize, indexSize, 256, 256
                )
                if (isMouseOverIndex(mouseX, mouseY)) {
                    setTooltip(slot.tooltip)
                }
            }
            hovered = isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        }

        fun isMouseOverIndex(mouseX: Int, mouseY: Int): Boolean =
            visible && mouseX >= indexX && mouseY >= indexY && mouseX < indexX + indexSize && mouseY < indexY + indexSize
    }

    inner class SlotGrid(
        val x: Int,
        val y: Int,
        var rows: Int,
        var columns: Int,
        var gap: Int,
        var padding: Insets,
    ) : AbstractParentElement(), Drawable, Selectable {

        val slots: MutableList<Slot> = mutableListOf()
        var hoveredSlot: Slot? = null
        var scrollOffset: Int = 0
            set(value) {
                field = value.coerceIn(0, maxScrollAmount)
            }
        private var draggingScrollbar: Boolean = false
        val width: Int
            get() = columns * (SLOT_SIZE + gap) + padding.left + padding.right
        val contentHeight: Int
            get() = ((slots.size + columns - 1) / columns) * (SLOT_SIZE + gap) - gap + padding.top + padding.bottom
        val viewportHeight: Int
            get() = rows * SLOT_SIZE + (rows - 1) * gap + padding.top + padding.bottom
        private val maxScrollAmount: Int
            get() = (contentHeight - viewportHeight).coerceAtLeast(0)

        init {
            require(rows > 0 && columns > 0) { "rows and columns must be greater than 0" }
        }

        fun addSlot(skill: Skill) {
            slots.add(Slot(null, skill, 0, 0))
        }

        fun replaceSlots(skills: Collection<Skill>) {
            slots.clear()
            skills.forEach(::addSlot)
            val size = skills.size
            val totalSlots = rows * columns
            repeat(
                if (size < totalSlots) totalSlots - size else columns - (if (size % columns == 0) columns else size % columns)
            ) {
                addSlot(Skills.EMPTY)
            }
            scrollOffset = scrollOffset
        }

        override fun children(): MutableList<out Element> = slots

        override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
            mouseX.toInt() in x..x + width && mouseY.toInt() in y..y + viewportHeight

        fun isMouseOverScrollbar(mouseX: Int, mouseY: Int): Boolean =
            mouseX in x + width - 16..x + width - 10 && mouseY in y + padding.top..y + viewportHeight - padding.bottom

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            hoveredSlot = null
            // 计算可见行范围
            val startRow = (scrollOffset / (SLOT_SIZE + gap)).coerceAtLeast(0)
            val renderingSlots = slots.toList()
            val endRow = ((scrollOffset + viewportHeight) / (SLOT_SIZE + gap) + 1)
                .coerceAtMost((renderingSlots.size + columns - 1) / columns + 1)
            // 渲染可见插槽
            context.enableScissor(x, y + padding.top, x + width, y + viewportHeight - padding.bottom)
            for (index in (startRow * columns) until minOf((endRow * columns), renderingSlots.size)) {
                val slot = renderingSlots[index]
                slot.x = x + padding.left + (index % columns) * (SLOT_SIZE + gap)
                slot.y = y + padding.top + (index / columns) * (SLOT_SIZE + gap) - scrollOffset
                if (slot.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && !isOutOfBound(mouseX, mouseY)) {
                    slot.hovered = true
                    hoveredSlot = slot
                } else {
                    slot.hovered = false
                }
                slot.render(context, mouseX, mouseY, delta)
            }
            if (selectingSlot?.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) == true
                && isOutOfBound(mouseX, mouseY)
            ) {
                selectingSlot = null
            }
            context.disableScissor()
            // 渲染滚动条
            context.renderScrollbar(
                x + width - 16,
                y + padding.top,
                viewportHeight - padding.top - padding.bottom,
                scrollOffset,
                maxScrollAmount
            )
        }

        override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
            val scrollAmount = SLOT_SIZE + gap
            scrollOffset -= (amount * scrollAmount).toInt()
            return super.mouseScrolled(mouseX, mouseY, amount)
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (isMouseOverScrollbar(mouseX.toInt(), mouseY.toInt())) {
                draggingScrollbar = true
                return true
            }
            if (isOutOfBound(mouseX.toInt(), mouseY.toInt())) return false
            return super.mouseClicked(mouseX, mouseY, button)
        }

        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
            draggingScrollbar = false
            return super.mouseReleased(mouseX, mouseY, button)
        }

        override fun mouseDragged(
            mouseX: Double,
            mouseY: Double,
            button: Int,
            deltaX: Double,
            deltaY: Double,
        ): Boolean {
            if (draggingScrollbar) {
                val scrollbarHeight = viewportHeight - padding.top - padding.bottom
                val sliderRange = scrollbarHeight - Renderer2d.SLIDER_HEIGHT
                if (sliderRange > 0) {
                    // 鼠标 Y 坐标转为滑块位置
                    val relativeY = (mouseY - y - padding.top - 1).coerceIn(0.0, sliderRange.toDouble())
                    // 滑块位置按比例映射到 scrollAmount
                    scrollOffset = ((relativeY / sliderRange) * maxScrollAmount).toInt()
                }
                return true
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        }

        override fun hoveredElement(mouseX: Double, mouseY: Double): Optional<Element> {
            if (isOutOfBound(mouseX.toInt(), mouseY.toInt())) return Optional.empty()
            return super.hoveredElement(mouseX, mouseY)
        }

        fun isOutOfBound(mouseX: Int, mouseY: Int) =
            mouseX !in x + padding.left..x + width - padding.right || mouseY !in y + padding.top..y + viewportHeight - padding.bottom

        override fun appendNarrations(builder: NarrationMessageBuilder) = Unit
        override fun getType(): Selectable.SelectionType =
            if (this.isFocused) Selectable.SelectionType.FOCUSED
            else if (this.hoveredSlot != null) Selectable.SelectionType.HOVERED
            else Selectable.SelectionType.NONE
    }

    inner class Tab(
        val type: SkillType,
        val index: Int, x: Int, y: Int,
        var reverse: Boolean,
        displayIcon: Skill? = null,
    ) : ClickableWidget(x, y, 26, 32, type.displayName) {

        val selected
            get() = selectedTab?.type == this.type
        val v: Int
            get() {
                var v = 0
                if (reverse) v += 64
                if (selected) v += 32
                return v
            }
        private val displaySkill = displayIcon ?: type.representsSkill()

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.drawTexture(
                tabTexture, x, y, if (selected) 0 else -1, 26f, v.toFloat(), width,
                if (selected) height else height - 4, 256, 256
            )
            SkillRenderer.renderIcon(
                displaySkill,
                context,
                x + (width - 16) / 2,
                y + (height - 16) / 2 + if (reverse) -2 else 2
            )
            if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
                setTooltip(type.displayName)
            }
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) = Unit

        override fun onClick(mouseX: Double, mouseY: Double) {
            super.onClick(mouseX, mouseY)
            selectedTab = if (selectedTab != this) this else null
        }
    }

    companion object {

        private val tabTexture = Identifier("textures/gui/container/creative_inventory/tabs.png")
        private val slotTexture = id("slots.png")
        private val sortTexture = id("sort.png")
        private val filterTexture = id("filter.png")
        private val rarityFilterTexture = id("rarity_filter.png")
        private const val SLOT_SIZE = 24
    }
}