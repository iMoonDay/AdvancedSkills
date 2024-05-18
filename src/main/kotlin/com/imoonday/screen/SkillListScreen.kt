package com.imoonday.screen

import com.imoonday.screen.component.ShiftScrollContainer
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
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Util
import java.awt.Color

class SkillListScreen(
    val player: PlayerEntity,
) : BaseOwoScreen<FlowLayout>(), AutoSyncedScreen {

    var selectedSkill: Skill? = null
    var selectedSlot: Int? = null
    private val selectedSlotSkill: Skill?
        get() = selectedSlot?.let { player.getSkill(it) }
    private val slotLines = mutableListOf<SlotLine>()
    private val container
        get() = player.skillContainer
    private var slotGrid: GridLayout =
        Containers.grid(
            Sizing.fill(50),
            Sizing.fill(100),
            (container.slotSize / 2 + if (container.slotSize % 2 == 0) 0 else 1).coerceAtLeast(1),
            if (container.slotSize > 1) 2 else 1
        ).apply {
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            container.getAllSlots().forEach {
                val slotLine = SlotLine(it.index)
                val column = if ((it.index - 1) % 2 == 0) 0 else 1
                val row = (it.index - 1) / 2
                child(slotLine, row, column)
                slotLines.add(slotLine)
            }
            padding(Insets.horizontal(5))
        }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    private val learnButton = Components.button(translate("screen", "list.button.learn")) {
        client!!.setScreen(SkillLearningScreen(player) { SkillListScreen(player) })
    }.apply {
        active(!player.learnableData.isEmpty())
    }
    private val skillsFlow = Containers.verticalFlow(
        Sizing.fill(100),
        Sizing.content()
    )

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)
            .padding(Insets.of(5, 0, 5, 5))

        rootComponent.gap(3)

        rootComponent.child(
            Containers.verticalFlow(
                Sizing.fill(100),
                Sizing.content()
            ).apply {
                child(
                    Components.label(
                        translate(
                            "screen",
                            "list.level",
                            "${player.skillLevel % 100}${if (player.skillLevel > 100) " (+${player.skillLevel / 100})" else ""}"
                        )
                    )
                )
                child(Components.label(translate("screen", "list.exp", player.skillExp)))
                child(Containers.grid(Sizing.fill(100), Sizing.fill(80), 1, 2).apply {
                    gap(3)
                    child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100)).apply {
                        child(Containers.verticalScroll(Sizing.fill(50), Sizing.fill(100), skillsFlow.apply {
                            player.learnedSkills.forEach { child(SkillLine(it)) }
                        }).apply {
                            scrollbar(ScrollContainer.Scrollbar.vanilla())
                            padding(Insets.of(5))
                            surface(Surface.PANEL)
                        })
                    }, 0, 0)
                    child(Containers.verticalScroll(Sizing.content(), Sizing.fill(100), slotGrid).apply {
                        scrollbarThiccness(0)
                    }, 0, 1)
                })
                if (player.isCreative && player.hasPermissionLevel(4)) {
                    child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        fun createButton(text: Text, content: String, close: Boolean = false) =
                            Components.button(text) {
                                (player as ClientPlayerEntity).networkHandler.sendCommand("skills @s $content")
                                if (close) close()
                            }.apply {
                                tooltip(translate("screen", "list.button.tooltip"))
                            }
                        gap(5)
                        child(createButton(translate("screen", "list.button.learnAll"), "learn-all"))
                        child(createButton(translate("screen", "list.button.forgetAll"), "forget-all"))
                        child(createButton(translate("screen", "list.button.resetCooldown"), "reset-cooldown", true))
                    })
                }
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    positioning(Positioning.relative(100, 0))
                    gap(5)
                    child(learnButton)
                    child(Components.button(translate("screen", "list.button.inventory")) {
                        client!!.setScreen(SkillInventoryScreen(player) { SkillListScreen(player) })
                    })
                })
            })
    }

    override fun update(data: NbtCompound) {
        updateScreen()
    }

    fun updateScreen() {
        skillsFlow.run {
            if (skillsFlow.children().size != player.learnedSkills.size) {
                clearChildren()
                player.learnedSkills.forEach { child(SkillLine(it)) }
            }
        }
        player.equippedSkills.forEachIndexed { i, _ ->
            slotLines[i].updateSkill()
        }
        learnButton.active(!player.learnableData.isEmpty())
    }

    override fun shouldPause(): Boolean = false

    inner class SkillLine(
        private val skill: Skill,
    ) : FlowLayout(Sizing.fill(98), Sizing.content(5), Algorithm.HORIZONTAL) {

        private val content: FlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        private val equipButton = Components.texture(com.imoonday.util.id("equip.png"), 0, 0, 16, 16, 16, 16).apply {
            mouseDown().subscribe { _, _, button ->
                if (button == 0) {
                    return@subscribe getValidSlot()?.run {
                        player.equip(skill, this)
                        selectedSlot = null
                        true
                    } ?: false
                }
                false
            }
        }

        private fun getValidSlot(): Int? {
            val emptyIndex = container.getEmptySlot(skill)?.index

            return when {
                selectedSlotSkill != null && player.getSlot(selectedSlot!!)?.canEquip(skill) == true -> selectedSlot
                emptyIndex != null && container.getSlot(skill) == null -> emptyIndex
                else -> null
            }
        }

        private var lastClickTime: Long = 0

        init {
            gap(5)
            surface(Surface.PANEL_INSET)
            content.gap(7)
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .padding(Insets.of(5, 5, 2, 0))
            child(content)
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            padding(Insets.horizontal(5))

            content.child(Components.texture(skill.icon, 0, 0, 16, 16, 16, 16))
            content.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                gap(5)
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    gap(5)
                    child(Components.label(skill.formattedName))
                    child(Components.label("(${skill.getCooldown(client?.world) / 20.0}s)".toText()))
                })
                child(
                    ShiftScrollContainer.horizontalScroll(
                        Sizing.fill(85),
                        Sizing.content(),
                        Components.label(skill.description)
                    ).apply {
                        scrollbarThiccness(0)
                        mouseDown().subscribe { _, _, button ->
                            onMouseDown(button)
                        }
                    }
                )
            })
            child(equipButton.apply {
                positioning(Positioning.relative(100, 50))
            })

            mouseDown().subscribe { _, _, button ->
                onMouseDown(button)
            }
        }

        private fun onMouseDown(button: Int): Boolean {
            if (button != 0) return false
            if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
                client!!.setScreen(
                    SkillGalleryScreen(
                        skill
                    ) { this@SkillListScreen }
                )
            }
            lastClickTime = Util.getMeasuringTimeMs()
            selectedSkill = if (selectedSkill != skill) skill else null
            updateScreen()
            return true
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            if (hovered && getValidSlot() != null) {
                equipButton.sizing(Sizing.content())
            } else {
                equipButton.sizing(Sizing.fill(0))
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (selectedSkill == skill || hovered) {
                context.fill(
                    x,
                    y,
                    x + width,
                    y + height,
                    Color.WHITE.alpha(if (selectedSkill == skill) 0.4 else 0.2).rgb
                )
            }
        }
    }

    inner class SlotLine(
        private val slot: Int,
    ) : FlowLayout(
        Sizing.fill(47),
        Sizing.fill(17),
        Algorithm.HORIZONTAL
    ) {

        var skill: Skill
            get() = player.getSkill(slot)
            set(value) {
                player.equip(value, slot)
            }
        private val content: FlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        private var lastClickTime: Long = 0

        init {
            gap(5)
            surface(Surface.TOOLTIP.and { context, component ->
                context.drawTexture(
                    indexTexture,
                    component.x() + component.width() - 13,
                    component.y() + 4,
                    player.getSlot(slot)?.u ?: 0,
                    player.getSlot(slot)?.v ?: 0,
                    9,
                    9
                )
            })
            content.gap(5)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

            padding(Insets.left(8))
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)

            updateSkill()

            mouseDown().subscribe { _, _, button ->
                onMouseDown(button)
            }

            player.getSlot(slot)?.let {
                tooltip(it.tooltip)
            }
        }

        private fun onMouseDown(button: Int): Boolean {
            if (button != 0) return false
            if (!skill.invalid && Util.getMeasuringTimeMs() - lastClickTime < 250L) {
                player.equip(Skill.EMPTY, slot)
            }
            lastClickTime = Util.getMeasuringTimeMs()
            selectedSlot = if (selectedSlot != slot) slot else null
            updateScreen()
            return true
        }

        fun updateSkill() {
            clearChildren()
            if (!this.skill.invalid) {
                content.clearChildren()
                child(content)

                content.child(Components.texture(this.skill.icon, 0, 0, 16, 16, 16, 16))
                content.child(Components.label(this@SlotLine.skill.formattedName))
            }
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (selectedSlot == slot || hovered) {
                context.fill(
                    x + 3,
                    y + 3,
                    x + width - 3,
                    y + height - 3,
                    Color.WHITE.alpha(if (selectedSkill == skill) 0.4 else 0.2).rgb
                )
            }
        }

        override fun shouldDrawTooltip(mouseX: Double, mouseY: Double): Boolean =
            this.tooltip() != null && mouseX >= x + width - 13 && mouseX <= x + width - 4 && mouseY >= y + 4 && mouseY <= y + 13
    }
}