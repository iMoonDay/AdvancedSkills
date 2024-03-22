package com.imoonday.screen

import com.imoonday.skill.Skill
import com.imoonday.util.*
import com.imoonday.util.SkillSlot.Companion.indexTexture
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.awt.Color

class SkillInventoryScreen(
    val player: PlayerEntity,
    val parent: Screen? = null,
) : BaseOwoScreen<FlowLayout>(), AutoSyncedScreen {

    var selectedSlot: Slot? = null
        set(value) {
            field = value
            update()
        }
    var selectedTab: Tab? = null
        set(value) {
            field = value
            scrollContainer.onMouseScroll(0.0, 0.0, 100.0)
            update()
        }
    var selectingSlot: Slot? = null
    private val tabs: MutableList<Tab> = mutableListOf()
    private val equippedSlots: MutableList<Slot> = mutableListOf()
    private lateinit var inventory: GridLayout
    private lateinit var scrollContainer: ScrollContainer<*>
    private val slotSize = 24
    private val tabTexture = Identifier("textures/gui/container/creative_inventory/tabs.png")
    private val titleComponent = Components.label(
        translate(
            "screen",
            "inventory.title",
            player.learnedSkills.size,
            Skill.getValidSkills().size
        ).formatted(Formatting.BLACK)
    )
    private val rows: Int
        get() = Skill.getValidSkills().size / 9 + if (Skill.getValidSkills().size % 9 == 0) 0 else 1
    private val displaySkills
        get() = player.learnedSkills
            .filterNot { player.hasEquipped(it) }
            .filter { skill ->
                (selectedTab?.type ?: return@filter true) in skill.types
            }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!
    override fun build(rootComponent: FlowLayout) {
        setupRootComponent(rootComponent)
        setupInventory(rootComponent)
    }

    private fun setupRootComponent(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .padding(Insets.of(5, 2, 5, 5))
    }

    private fun setupInventory(rootComponent: FlowLayout) {
        rootComponent.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
            horizontalAlignment(HorizontalAlignment.CENTER)
            val halfSize = SkillType.entries.size / 2
            val width = 9 * (slotSize + 4) + 15
            addTopTabs(width, halfSize)
            addSkills(width)
            addBottomTabs(width, halfSize)
        })
    }

    private fun FlowLayout.addBottomTabs(width: Int, halfSize: Int) {
        child(Containers.grid(Sizing.fixed(width), Sizing.content(), 1, 5).apply {
            horizontalAlignment(HorizontalAlignment.CENTER)
            SkillType.entries.drop(halfSize).forEachIndexed { index, skillType ->
                child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    val tab = Tab(skillType, halfSize + index)
                    child(tab)
                    tabs.add(tab)
                }, 0, index)
            }
        })
    }

    private fun FlowLayout.addSkills(width: Int) {
        child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
            surface(Surface.PANEL.and { context, component ->
                selectedTab?.run {
                    context.drawTexture(
                        tabTexture,
                        x(),
                        if (reverse) component.y() + component.height() - 4 else component.y(),
                        26,
                        v + if (reverse) 0 else 28,
                        26,
                        4
                    )
                }
            })
            padding(Insets.vertical(7))
            horizontalAlignment(HorizontalAlignment.CENTER)
            gap(3)
            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                child(titleComponent)
            })
            val skills = displaySkills
            val rows = rows
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                gap(3)
                child(Containers.verticalScroll(
                    Sizing.content(),
                    Sizing.fixed(slotSize * 5),
                    Containers.grid(Sizing.content(), Sizing.content(), rows, 9).apply {
                        padding(Insets.right(5))
                        alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                        for (index in (0 until rows * 9)) {
                            child(
                                Slot(null, skills.getOrElse(index) { Skill.EMPTY }),
                                index / 9,
                                index % 9
                            )
                        }
                    }.also { inventory = it }
                ).apply {
                    scrollbar(ScrollContainer.Scrollbar.vanilla())
                    padding(Insets.horizontal(5))
                }.also { scrollContainer = it })
                val slots = player.skillContainer.getAllSlots()
                child(
                    Containers.horizontalScroll(
                        Sizing.fixed(width - 15),
                        Sizing.content(),
                        Containers.grid(Sizing.content(), Sizing.content(), 1, slots.size).apply {
                            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                            slots.forEach {
                                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                                    padding(Insets.of(1))
                                    alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                                    gap(3)
                                    child(Components.texture(indexTexture, it.u, it.v, 9, 9).apply {
                                        tooltip(it.tooltip)
                                    })
                                    val slot = Slot(it, it.skill)
                                    child(slot)
                                    equippedSlots.add(slot)
                                }, 0, it.index - 1)
                            }
                        }).apply {
                        scrollbarThiccness(0)
                        scrollStep(42)
                    }
                )
            })
        })
    }

    private fun FlowLayout.addTopTabs(width: Int, halfSize: Int) {
        child(Containers.grid(Sizing.fixed(width), Sizing.content(), 1, 5).apply {
            horizontalAlignment(HorizontalAlignment.CENTER)
            SkillType.entries.take(halfSize).forEachIndexed { index, skillType ->
                child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    val tab = Tab(skillType, index)
                    child(tab, 0, index)
                    tabs.add(tab)
                })
            }
        })
    }

    override fun update(data: NbtCompound) {
        val size = inventory.children().size
        val skills = displaySkills
        inventory.run {
            for (index in 0 until size) {
                child(
                    Slot(null, skills.getOrElse(index) { Skill.EMPTY }),
                    index / 9,
                    index % 9
                )
            }
        }
        player.equippedSkills.forEachIndexed { index, skill ->
            equippedSlots[index].updateSkill(skill)
        }
        titleComponent.text(
            (if (selectedTab == null) translate(
                "screen",
                "inventory.title",
                player.learnedSkills.size,
                Skill.getValidSkills().size
            ) else selectedTab!!.type.displayName).copy().formatted(Formatting.BLACK)
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        selectedSlot?.run {
            context.drawTexture(skill.icon, mouseX - 8, mouseY - 8, 90, 0f, 0f, 16, 16, 16, 16)
        }
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

    override fun close() = client!!.setScreen(parent)

    override fun init() {
        super.init()
        tabs.forEach {
            it.updateY(it.y() + if (it.reverse) -4 else 4)
        }
    }

    override fun shouldPause(): Boolean = false

    inner class Slot(
        val slot: SkillSlot?,
        var skill: Skill,
    ) : FlowLayout(Sizing.fixed(slotSize), Sizing.fixed(slotSize), Algorithm.VERTICAL) {

        private val slotTexture = com.imoonday.util.id("slot.png")

        init {
            padding(Insets.of(1))
            surface { context, component ->
                context.drawTexture(
                    slotTexture,
                    component.x(),
                    component.y(),
                    skill.rarity.level * 24f,
                    0f,
                    component.width(),
                    component.height(),
                    256,
                    256
                )
                context.fill(
                    component.x() + component.width() - 3,
                    component.y() + 1,
                    component.x() + component.width() - 1,
                    component.y() + 3,
                    skill.rarity.formatting.colorValue ?: 0
                )
            }
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
            margins(Insets.of(2))
            updateSkill(skill)
            mouseDown().subscribe { _, _, button ->
                if (button != 0) return@subscribe false
                if (slot == null) {
                    if (hasShiftDown()) {
                        player.equip(skill)
                    } else {
                        if (selectedSlot != null && selectedSlot!!.slot != null) {
                            player.equip(Skill.EMPTY, selectedSlot!!.slot!!)
                            selectedSlot = null
                        } else {
                            selectedSlot = if (selectedSlot == null && !skill.invalid) this else null
                        }
                    }
                } else if (selectedSlot != null && selectedSlot != this) {
                    if (!slot.canEquip(selectedSlot!!.skill)) return@subscribe false
                    player.equip(selectedSlot!!.skill, slot)
                    if (selectedSlot!!.slot != null && !selectedSlot!!.skill.invalid) player.equip(
                        skill,
                        selectedSlot!!.slot!!
                    )
                    selectedSlot = null
                } else if (!skill.invalid) {
                    if (hasShiftDown()) {
                        player.equip(Skill.EMPTY, slot)
                    } else {
                        selectedSlot = if (selectedSlot != this) this else null
                    }
                }
                true
            }
        }

        fun updateSkill(skill: Skill) {
            this.skill = skill
            clearChildren()
            skill.takeUnless { it.invalid || this == selectedSlot }?.run {
                child(Components.texture(icon, 0, 0, 16, 16, 16, 16))
            }
            tooltip(skill.name)
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            if (hovered) selectingSlot = if (!skill.invalid) this else null
            else if (selectingSlot == this) selectingSlot = null
            if (hasShiftDown()) tooltip(skill.getItemTooltips(client!!, true)) else tooltip(skill.name)
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (hovered) {
                val edge = 4
                context.fill(
                    x + edge,
                    y + edge,
                    x + width - edge,
                    y + height - edge,
                    Color.WHITE.alpha(0.4).rgb
                )
            }
        }

        override fun shouldDrawTooltip(mouseX: Double, mouseY: Double): Boolean =
            super.shouldDrawTooltip(mouseX, mouseY) && selectedSlot == null && !skill.invalid

        override fun equals(other: Any?): Boolean = other is Slot && other.skill == skill
        override fun hashCode(): Int {
            var result = slot.hashCode()
            result = 31 * result + skill.hashCode()
            return result
        }
    }

    inner class Tab(
        val type: SkillType,
        val index: Int,
    ) : FlowLayout(Sizing.fixed(26), Sizing.fixed(32), Algorithm.HORIZONTAL) {

        val selected
            get() = selectedTab == this
        val reverse
            get() = index > SkillType.entries.size / 2 - 1
        val v: Int
            get() {
                var v = 0
                if (reverse) v += 64
                if (selected) v += 32
                return v
            }

        init {
            surface { context, component ->
                context.drawTexture(
                    tabTexture,
                    component.x(),
                    component.y(),
                    26,
                    v,
                    component.width(),
                    component.height()
                )
            }
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
            tooltip(type.displayName)
            mouseDown().subscribe { _, _, button ->
                if (button != 0) return@subscribe false
                selectedTab = if (selectedTab != this) this else null
                client!!.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
                true
            }
            child(
                Containers.verticalFlow(Sizing.content(), Sizing.fixed(32)).apply {
                    if (!reverse) {
                        padding(Insets.top(2))
                    } else {
                        padding(Insets.bottom(2))
                    }
                    alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    child(
                        Components.texture(
                            Skill.getValidSkills().first { type in it.types }.icon,
                            0,
                            0,
                            16,
                            16,
                            16,
                            16
                        )
                    )
                }
            )
        }
    }
}