package com.imoonday.screen

import com.imoonday.component.equipSkill
import com.imoonday.component.equippedSkills
import com.imoonday.component.learnedSkills
import com.imoonday.init.ModSkills
import com.imoonday.skill.Skill
import com.imoonday.util.*
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.awt.Color

class SkillInventoryScreen(
    val player: PlayerEntity,
    val parent: Screen? = null,
) : BaseOwoScreen<FlowLayout>(), AutoSyncedScreen {

    var selectedSlot: Slot? = null
        set(value) {
            field = value
            update(NbtCompound())
        }
    var selectedTab: Tab? = null
        set(value) {
            field = value
            scrollContainer.onMouseScroll(0.0, 0.0, 100.0)
            update(NbtCompound())
        }
    val tabs: MutableList<Tab> = mutableListOf()
    private val equippedSlots: MutableList<Slot> = mutableListOf()
    private lateinit var inventory: GridLayout
    private lateinit var scrollContainer: ScrollContainer<*>
    private val slotSize = 18
    private val rows: Int
        get() = ModSkills.SKILLS.size / 9 + if (ModSkills.SKILLS.size % 9 == 0) 0 else 1
    private val tabTexture = Identifier("textures/gui/container/creative_inventory/tabs.png")
    private val titleComponent = Components.label(
        translate(
            "screen",
            "inventory.title",
            player.learnedSkills.size,
            ModSkills.SKILLS.filterNot { it.invalid }.size
        ).formatted(Formatting.BLACK)
    )

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
            gap(0)
            val halfSize = SkillType.entries.size / 2
            child(Containers.grid(Sizing.fixed(9 * slotSize + 10), Sizing.content(), 1, 5).apply {
                horizontalAlignment(HorizontalAlignment.CENTER)
                SkillType.entries.take(halfSize).forEachIndexed { index, skillType ->
                    child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                        val tab = Tab(skillType, index)
                        child(tab, 0, index)
                        tabs.add(tab)
                    })
                }
            })
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
                val skills = player.learnedSkills.filterNot { it in player.equippedSkills }.toList()
                val rows = rows
                child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    gap(3)
                    zIndex(-1)
                    child(Containers.verticalScroll(
                        Sizing.content(),
                        Sizing.fixed(slotSize * 5),
                        Containers.grid(Sizing.content(), Sizing.content(), rows, 9).apply {
                            padding(Insets.right(5))
                            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                            for (index in (0 until rows * 9)) {
                                child(
                                    Slot(SkillSlot.INVALID, skills.getOrElse(index) { ModSkills.EMPTY }),
                                    index / 9,
                                    index % 9
                                )
                            }
                        }.also { inventory = it }
                    ).apply {
                        scrollbar(ScrollContainer.Scrollbar.vanilla())
                        scrollStep(slotSize)
                        padding(Insets.horizontal(5))
                    }.also { scrollContainer = it })
                    child(Containers.grid(Sizing.fixed(9 * slotSize + 5), Sizing.content(), 1, 4).apply {
                        padding(Insets.left(5))
                        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                        player.equippedSkills.forEachIndexed { index, skill ->
                            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                                padding(Insets.of(1))
                                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                                gap(3)
                                child(Components.label(Text.literal("${index + 1}").formatted(Formatting.BLACK)))
                                val slot = Slot(SkillSlot.fromIndex(index + 1), skill)
                                child(slot)
                                equippedSlots.add(slot)
                            }, 0, index)
                        }
                    })
                })
            })
            child(Containers.grid(Sizing.fixed(9 * slotSize + 10), Sizing.content(), 1, 5).apply {
                horizontalAlignment(HorizontalAlignment.CENTER)
                SkillType.entries.drop(halfSize).forEachIndexed { index, skillType ->
                    child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                        val tab = Tab(skillType, halfSize + index)
                        child(tab)
                        tabs.add(tab)
                    }, 0, index)
                }
            })
        })
    }

    override fun update(data: NbtCompound) {
        val size = inventory.children().size
        val skills =
            player.learnedSkills.filterNot { it in player.equippedSkills || it == selectedSlot?.skill }.filter {
                it.types.any {
                    it == (selectedTab?.type ?: return@filter true)
                }
            }
        inventory.run {
            for (index in 0 until size) {
                child(
                    Slot(SkillSlot.INVALID, skills.getOrElse(index) { ModSkills.EMPTY }),
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
                ModSkills.SKILLS.filterNot { it.invalid }.size
            ) else selectedTab!!.type.displayName).copy().formatted(Formatting.BLACK)
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        selectedSlot?.run {
            context.drawTexture(skill.icon, mouseX - 8, mouseY - 8, 90, 0f, 0f, 16, 16, 16, 16)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean =
        if (client!!.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            close()
            true
        } else super.keyPressed(keyCode, scanCode, modifiers)

    override fun close() = client!!.setScreen(parent)
    override fun init() {
        super.init()
        tabs.forEach {
            it.updateY(it.y() + if (it.reverse) -4 else 4)
        }
    }

    inner class Slot(
        val slot: SkillSlot,
        var skill: Skill,
    ) : FlowLayout(Sizing.fixed(slotSize), Sizing.fixed(slotSize), Algorithm.VERTICAL) {

        init {
            padding(Insets.of(1))
            surface(Surface.tiled(com.imoonday.util.id("slot.png"), slotSize, slotSize))
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
            updateSkill(skill)
            mouseDown().subscribe { _, _, button ->
                if (button != 0) return@subscribe false
                if (!slot.valid) {
                    if (hasShiftDown()) {
                        if (equippedSlots.any { it.skill.invalid }) {
                            player.equipSkill(
                                skill,
                                SkillSlot.fromIndex(player.equippedSkills.indexOfFirst { it.invalid } + 1)
                            )
                        }
                    } else {
                        if (selectedSlot != null && selectedSlot!!.slot.valid) {
                            player.equipSkill(ModSkills.EMPTY, selectedSlot!!.slot)
                            selectedSlot = null
                        } else {
                            selectedSlot = if (selectedSlot == null && !skill.invalid) this else null
                        }
                    }
                } else if (selectedSlot != null && selectedSlot != this) {
                    player.equipSkill(selectedSlot!!.skill, slot)
                    if (selectedSlot!!.slot.valid && !selectedSlot!!.skill.invalid) player.equipSkill(
                        skill,
                        selectedSlot!!.slot
                    )
                    selectedSlot = null
                } else if (!skill.invalid) {
                    if (hasShiftDown()) {
                        player.equipSkill(ModSkills.EMPTY, slot)
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
            skill.takeUnless { it.invalid || slot.valid && this == selectedSlot }?.run {
                child(Components.texture(icon, 0, 0, 16, 16, 16, 16))
            }
            tooltip(skill.name)
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            if (hasShiftDown()) {
                tooltip(skill.getItemTooltips(client!!, true))
            } else {
                tooltip(skill.name)
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (hovered) {
                context.fill(
                    x + 1,
                    y + 1,
                    x + width - 1,
                    y + height - 1,
                    Color.WHITE.alpha(0.4).rgb
                )
            }
        }

        override fun shouldDrawTooltip(mouseX: Double, mouseY: Double): Boolean =
            super.shouldDrawTooltip(mouseX, mouseY) && selectedSlot == null && !skill.invalid
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
                    child(Components.texture(
                        ModSkills.SKILLS.filterNot { it.invalid }.first { type in it.types }.icon,
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