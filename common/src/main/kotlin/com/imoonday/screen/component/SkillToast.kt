package com.imoonday.screen.component

import com.imoonday.skill.*
import com.imoonday.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.toast.*
import net.minecraft.util.*

private val texture = id("toasts.png")

class SkillToast(val skill: Skill, private val timeout: Long = 5000) : Toast {

    override fun draw(context: DrawContext, manager: ToastManager, startTime: Long): Toast.Visibility {
        context.drawTexture(texture, 0, 0, 0f, (skill.rarity.level - 1).coerceAtLeast(0) * 32f, 160, 32, 256, 256)
        skill.renderIcon(context, 8, 8)
        val textRenderer = manager.client.textRenderer
        context.drawText(
            textRenderer,
            translate("learnSkill", "toast").copy().formatted(Formatting.YELLOW),
            30,
            6,
            0xFFFFFF,
            false
        )
        context.drawText(textRenderer, skill.name.copy().formatted(Formatting.WHITE), 30, 18, 0xFFFFFF, false)
        return if (startTime < timeout) Toast.Visibility.SHOW else Toast.Visibility.HIDE
    }
}

