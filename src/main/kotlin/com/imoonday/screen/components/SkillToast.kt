package com.imoonday.screen.components

import com.imoonday.utils.Skill
import io.wispforest.owo.ui.base.BaseOwoToast
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.toast.Toast
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class SkillToast(val skill: Skill, timeout: Long = 5000) : BaseOwoToast<FlowLayout>(
    {
        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            gap(5)
            padding(Insets.of(5))
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            surface(Surface.VANILLA_TRANSLUCENT)
            child(Components.texture(skill.icon, 0, 0, 16, 16, 16, 16))
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                gap(2)
                alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                child(
                    Components.label(
                        Text.translatable("advancedSkills.learnSkill.toast").formatted(Formatting.YELLOW)
                    )
                )
                child(Components.label(skill.name.copy().formatted(Formatting.WHITE)))
            })
        }
    }, { _, time ->
        if (time >= timeout) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    }
)