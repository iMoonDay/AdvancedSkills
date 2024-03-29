package com.imoonday.screen

import com.imoonday.skill.Skill
import com.imoonday.util.alpha
import com.imoonday.util.translate
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color

class SkillGalleryScreen(private val parent: Screen? = null, private val positioning: Skill? = null) :
    BaseOwoScreen<FlowLayout>() {

    var selectedSkill: Skill? = null
        set(value) {
            field = value
            refreshInfoBox()
        }
    private val skillScroll: ScrollContainer<FlowLayout> =
        Containers.verticalScroll(Sizing.fill(55), Sizing.fill(100), Containers.verticalFlow(
            Sizing.fill(100),
            Sizing.content()
        ).apply {
            padding(Insets.right(1))
            Skill.getValidSkills().forEachIndexed { i: Int, skill: Skill ->
                child(SkillLine(i + 1, skill))
            }
        }).apply {
            scrollbar(ScrollContainer.Scrollbar.vanilla())
            surface(Surface.PANEL)
            padding(Insets.of(5))
        }
    private val infoBox: FlowLayout = Containers.verticalFlow(Sizing.fill(37), Sizing.content()).apply {
        gap(5)
        horizontalAlignment(HorizontalAlignment.LEFT)
        verticalAlignment(VerticalAlignment.TOP)
        surface(Surface.TOOLTIP)
        padding(Insets.of(5))
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)
            .padding(Insets.of(5))

        rootComponent.gap(3)

        rootComponent.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
            horizontalAlignment(HorizontalAlignment.CENTER)
            child(Components.label(translate("screen", "gallery.title")))
        })
        rootComponent.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(95)).apply {
            horizontalAlignment(HorizontalAlignment.CENTER)
            child(skillScroll)
            child(infoBox)
        })
    }

    override fun init() {
        super.init()
        positioning?.let {
            scrollTo(it)
            selectedSkill = it
        }
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val skill = selectedSkill
        super.resize(client, width, height)
        client.setScreen(SkillGalleryScreen(parent, skill))
    }

    fun refreshInfoBox() {
        infoBox.run {
            clearChildren()
            selectedSkill?.let {
                child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                    alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    child(Components.texture(it.icon, 0, 0, 16, 16, 16, 16))
                })
                child(Components.label(translate("screen", "gallery.info.name", it.name.string)).trim())
                child(
                    Components.label(translate("screen", "gallery.info.type", it.types.joinToString(" ") { type -> type.displayName.string }))
                        .trim()
                )
                child(Components.label(translate("screen", "gallery.info.description", it.description.string)).trim())
                child(
                    Components.label(
                        translate(
                            "screen",
                            "gallery.info.cooldown",
                            "${it.getCooldown(client?.world) / 20.0}s"
                        )
                    ).trim()
                )
                child(Components.label(translate("screen", "gallery.info.rarity", it.rarity.displayName.string)).trim())
            }
        }
    }

    private fun LabelComponent.trim() = apply { horizontalSizing(Sizing.fill(100)) }

    override fun close() {
        client!!.setScreen(parent)
    }

    fun scrollTo(skill: Skill) {
        (skillScroll.children().first() as FlowLayout).children().find { it is SkillLine && it.skill == skill }?.let {
            skillScroll.scrollTo(it)
        }
    }

    inner class SkillLine(
        index: Int,
        val skill: Skill,
    ) : FlowLayout(Sizing.fill(98), Sizing.content(2), Algorithm.HORIZONTAL) {

        private val content: FlowLayout = Containers.horizontalFlow(Sizing.fill(95), Sizing.content())

        init {
            gap(5)
            surface(Surface.PANEL_INSET)
            content.gap(5)
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .padding(Insets.of(5))
            child(content)
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

            content.child(Components.label(Text.literal(index.toString().padStart(3, '0'))))
            content.child(Components.texture(skill.icon, 0, 0, 16, 16, 16, 16))
            content.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                gap(2)
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    gap(5)
                    child(Components.label(skill.formattedName))
                })
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    gap(5)
                    child(Components.label(skill.rarity.displayName))
                    child(Components.label(Text.literal("|")))
                    child(Components.label(Text.literal(skill.types.joinToString(" ") { it.displayName.string })))
                })
            })

            mouseDown().subscribe { _, _, _ ->
                selectedSkill = skill
                true
            }
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (selectedSkill == skill || hovered) {
                context.fill(x, y, x + width, y + height, Color.WHITE.alpha(if (selectedSkill == skill) 0.4 else 0.2).rgb)
            }
        }
    }
}