package com.imoonday.screen

import com.imoonday.component.*
import com.imoonday.init.ModSkills
import com.imoonday.screen.component.ShiftScrollContainer
import com.imoonday.skill.Skill
import com.imoonday.util.AutoSyncedScreen
import com.imoonday.util.SkillSlot
import com.imoonday.util.alpha
import com.imoonday.util.translate
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
    var selectedSlot: SkillSlot? = null
    private val selectedSlotSkill: Skill?
        get() = selectedSlot?.let { player.equippedSkills[it.ordinal - 1] }
    private val slotLines = mutableListOf<SlotLine>()
    private var slotGrid: GridLayout = Containers.grid(Sizing.fill(50), Sizing.fill(100), 4, 1).apply {
        player.equippedSkills.forEachIndexed { i, skill ->
            val slotLine = SlotLine(SkillSlot.fromIndex(i + 1), skill)
            child(slotLine, 0, i)
            slotLines.add(slotLine)
        }
        padding(Insets.horizontal(5))
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)
            .padding(Insets.of(5, 0, 5, 5))

        rootComponent.gap(3)

        rootComponent.child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
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
                    child(Containers.verticalScroll(Sizing.fill(50), Sizing.fill(100), Containers.verticalFlow(
                        Sizing.fill(100),
                        Sizing.content()
                    ).apply {
                        player.learnedSkills.filterNot { it.invalid }
                            .forEach { child(SkillLine(it)) }
                    }).apply {
                        scrollbar(ScrollContainer.Scrollbar.vanilla())
                        padding(Insets.of(5))
                        surface(Surface.PANEL)
                    })
                }, 0, 0)
                child(slotGrid, 0, 1)
            })
            if (player.isCreative && player.hasPermissionLevel(4)) {
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    fun createButton(text: Text, content: String) =
                        Components.button(text) {
                            (player as ClientPlayerEntity).networkHandler.sendCommand("skills @s $content")
                            close()
                        }.apply {
                            tooltip(translate("screen", "list.button.tooltip"))
                        }
                    gap(5)
                    child(createButton(translate("screen", "list.button.learnAll"), "learn-all"))
                    child(createButton(translate("screen", "list.button.forgetAll"), "forget-all"))
                    child(createButton(translate("screen", "list.button.resetCooldown"), "reset-cooldown"))
                })
            }
            child(Components.button(translate("screen", "list.button.inventory")) {
                client!!.setScreen(SkillInventoryScreen(player, this@SkillListScreen))
            }.apply {
                positioning(Positioning.relative(100, 0))
            })
        })
    }

    override fun update(data: NbtCompound) {
        updateScreen()
    }

    fun updateScreen() {
        player.equippedSkills.forEachIndexed { i, skill ->
            slotLines[i].updateSkill(skill)
        }
    }

    inner class SkillLine(
        private val skill: Skill,
    ) : FlowLayout(Sizing.fill(98), Sizing.content(5), Algorithm.HORIZONTAL) {

        private val content: FlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        private val equipButton = Components.texture(com.imoonday.util.id("equip.png"), 0, 0, 16, 16, 16, 16).apply {
            mouseDown().subscribe { x, y, button ->
                if (button == 0) {
                    val slot = getValidSlot()
                    return@subscribe if (slot != SkillSlot.INVALID) {
                        player.equipSkill(skill, slot)
                        selectedSlot = null
                        true
                    } else false
                }
                false
            }
        }

        private fun getValidSlot(): SkillSlot {
            val skills = player.equippedSkills
            return if (selectedSlotSkill != null && selectedSlotSkill != skill || skills.any { it.invalid } && skills.none { it == skill }) selectedSlot
                ?: SkillSlot.fromIndex(skills.indexOfFirst { it.invalid } + 1) else SkillSlot.INVALID
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
                    child(Components.label(Text.literal("(${skill.getCooldown(client?.world) / 20.0}s)")))
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
                        this@SkillListScreen,
                        skill
                    )
                )
            }
            lastClickTime = Util.getMeasuringTimeMs()
            selectedSkill = if (selectedSkill != skill) skill else null
            updateScreen()
            return true
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            if (hovered && getValidSlot() != SkillSlot.INVALID) {
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
        private val slot: SkillSlot,
        var skill: Skill,
    ) : FlowLayout(Sizing.fill(100), Sizing.fill(24), Algorithm.HORIZONTAL) {

        private val content: FlowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        private var lastClickTime: Long = 0

        init {
            gap(5)
            surface(Surface.TOOLTIP)
            content.gap(5)
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .padding(Insets.of(3, 2, 8, 0))

            verticalAlignment(VerticalAlignment.CENTER)

            updateSkill()

            mouseDown().subscribe { _, _, button ->
                onMouseDown(button)
            }
        }

        private fun onMouseDown(button: Int): Boolean {
            if (button != 0) return false
            if (!skill.invalid && Util.getMeasuringTimeMs() - lastClickTime < 250L) {
                player.equipSkill(ModSkills.EMPTY, slot)
            }
            lastClickTime = Util.getMeasuringTimeMs()
            selectedSlot = if (selectedSlot != slot) slot else null
            updateScreen()
            return true
        }

        fun updateSkill(skill: Skill? = null) {
            if (this.skill == skill) return
            this.skill = skill ?: this.skill
            clearChildren()
            if (!this.skill.invalid) {
                content.clearChildren()
                child(content)

                content.child(Components.texture(this.skill.icon, 0, 0, 16, 16, 16, 16))
                content.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    gap(3)
                    child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        gap(5)
                        child(Components.label(this@SlotLine.skill.formattedName))
                        child(Components.label(Text.literal("(${this@SlotLine.skill.getCooldown(client?.world) / 20.0}s)")))
                    })
                    child(
                        ShiftScrollContainer.horizontalScroll(
                            Sizing.fill(85),
                            Sizing.content(),
                            Components.label(this@SlotLine.skill.description)
                        ).apply {
                            scrollbarThiccness(0)
                        }
                    )
                })
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
    }
}