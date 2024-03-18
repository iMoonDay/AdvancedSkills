package com.imoonday.screen.component

import com.imoonday.skill.Skill
import com.imoonday.util.id
import com.imoonday.util.translate
import io.wispforest.owo.ui.base.BaseOwoToast
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.client.toast.Toast
import net.minecraft.util.Formatting

private val texture = id("toasts.png")

class SkillToast(val skill: Skill, timeout: Long = 5000) : BaseOwoToast<FlowLayout>(
    {
        Containers.horizontalFlow(Sizing.fixed(160), Sizing.fixed(32)).apply {
            gap(7)
            padding(Insets.of(5).withLeft(7))
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            surface { context, component ->
                context.drawTexture(
                    texture,
                    component.x(),
                    component.y(),
                    0f,
                    (skill.rarity.level - 1).coerceAtLeast(0) * 32f,
                    component.width(),
                    component.height(),
                    256,
                    256
                )
            }
            child(Components.texture(skill.icon, 0, 0, 16, 16, 16, 16))
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                gap(2)
                alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                child(
                    Components.label(
                        translate("learnSkill", "toast").copy().formatted(Formatting.YELLOW)
                    )
                )
                child(Components.label(skill.name.copy().formatted(Formatting.WHITE)))
            })
        }
    }, { _, time ->
        if (time >= timeout) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    }
)