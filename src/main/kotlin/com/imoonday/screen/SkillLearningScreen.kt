package com.imoonday.screen

import com.imoonday.skill.Skill
import com.imoonday.util.*
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Util
import java.awt.Color

class SkillLearningScreen(
    val player: PlayerEntity,
    val parent: () -> Screen? = { null },
) : BaseOwoScreen<FlowLayout>(), AutoSyncedScreen {

    private val choice = player.getChoice()
    private var selectedBox: SkillBox? = null

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!
    private val refreshButton = Components.button(translate("screen", "learn.refresh")) {
        player.refreshChoice()
    }.apply {
        active(player.canFreshChoice())
    }
    private val learnButton =
        Components.button(translate("screen", "learn.learn")) {
            selectedBox?.choose()
        }.apply {
            active(selectedBox != null && !selectedBox!!.skill.invalid)
        }
    private val countLabel = Components.label(translate("screen", "learn.count", player.learnableData.count))

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)

        rootComponent.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
            gap(5)
            horizontalAlignment(HorizontalAlignment.CENTER)
            child(countLabel)
            child(Containers.grid(Sizing.content(), Sizing.content(), 1, 3).apply {
                child(SkillBox(choice.first) { player.chooseFirst() }, 0, 0)
                child(SkillBox(choice.second) { player.chooseSecond() }, 0, 1)
                child(SkillBox(choice.third) { player.chooseThird() }, 0, 2)
            })
            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(Containers.horizontalFlow(Sizing.fixed(150), Sizing.content()).apply {
                    alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    child(refreshButton)
                })
                child(Containers.horizontalFlow(Sizing.fixed(150), Sizing.content()).apply {
                    alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    child(learnButton)
                })
            })
        })

        new = false
    }

    override fun update(data: NbtCompound) {
        if (choice != player.learnableData.get()) {
            if (!player.learnableData.isEmpty())
                client!!.setScreen(
                    SkillLearningScreen(
                        player,
                        this@SkillLearningScreen.parent
                    )
                ) else close()
        }
        refreshButton.active(player.canFreshChoice())
        learnButton.active(selectedBox != null && !selectedBox!!.skill.invalid)
        countLabel.text(translate("screen", "learn.count", player.learnableData.count))
    }

    override fun close() = client!!.setScreen(parent())

    inner class SkillBox(val skill: Skill, private val chooseAction: () -> Unit) :
        FlowLayout(Sizing.fixed(100), Sizing.fill(65), Algorithm.VERTICAL) {

        private var lastClickTime = 0L

        init {
            surface(Surface.DARK_PANEL.and { context, component ->
                if (selectedBox == this)
                    context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), 0)
            })
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
            padding(Insets.both(8, 5))
            margins(Insets.horizontal(5))
            skill.takeUnless { it.invalid }?.let {
                child(
                    Containers.verticalScroll(Sizing.content(), Sizing.fill(100), Containers.verticalFlow(
                        Sizing.content(),
                        Sizing.content()
                    ).apply {
                        alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
                        gap(3)
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                            child(Components.texture(it.icon, 0, 0, 32, 32, 32, 32))
                        })
                        child(Components.label(translate("screen", "gallery.info.name", it.name.string)).trim())
                        child(
                            Components.label(
                                translate(
                                    "screen",
                                    "gallery.info.type",
                                    it.types.joinToString(" ") { type -> type.displayName.string })
                            )
                                .trim()
                        )
                        child(
                            Components.label(
                                translate(
                                    "screen",
                                    "gallery.info.description",
                                    it.description.string
                                )
                            ).trim()
                        )
                        child(
                            Components.label(
                                translate(
                                    "screen",
                                    "gallery.info.cooldown",
                                    "${it.getCooldown(client?.world) / 20.0}s"
                                )
                            ).trim()
                        )
                        child(
                            Components.label(
                                translate(
                                    "screen",
                                    "gallery.info.rarity",
                                    it.rarity.displayName.string
                                )
                            ).trim()
                        )
                    }).apply {
                        scrollbarThiccness(0)
                    }
                )
            }
            mouseDown().subscribe { _, _, button ->
                if (button == 0) {
                    selectedBox = if (selectedBox == this) null else this
                    update()
                    if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
                        choose()
                    }
                    lastClickTime = Util.getMeasuringTimeMs()
                    true
                } else false
            }
        }

        private fun LabelComponent.trim() = apply { horizontalSizing(Sizing.fill(100)) }

        fun choose() {
            chooseAction()
            if (player.learnableData.hasNext())
                client!!.setScreen(SkillLearningScreen(player, this@SkillLearningScreen.parent))
            else close()
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (selectedBox == this || hovered) {
                val edge = 3
                context.fill(
                    x + edge,
                    y + edge,
                    x + width - edge,
                    y + height - edge,
                    Color.WHITE.alpha(if (selectedBox == this) 0.4 else 0.2).rgb
                )
            }
        }
    }

    companion object {

        var new = false
    }
}