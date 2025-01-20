package com.imoonday.advskills_re.client.screen.component

import com.imoonday.advskills_re.client.render.skill.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*
import net.minecraft.client.toast.*
import net.minecraft.util.*

private val texture = id("textures/gui/toasts.png")

class SkillToast(val skill: Skill, private val timeout: Long = 3000) : Toast {

    override fun draw(context: DrawContext, manager: ToastManager, startTime: Long): Toast.Visibility {
        context.drawTexture(texture, 0, 0, 0f, (skill.rarity.level - 1).coerceAtLeast(0) * 32f, 160, 32, 256, 256)
        SkillRenderer.renderIcon(skill, context, 8, 8)
        val textRenderer = manager.client.textRenderer
        context.drawText(
            textRenderer,
            translate("learnSkill.toast").copy().formatted(Formatting.YELLOW),
            30,
            6,
            0xFFFFFF,
            false
        )
        context.drawText(textRenderer, skill.name.copy().formatted(Formatting.WHITE), 30, 18, 0xFFFFFF, false)
        return if (startTime < timeout * manager.notificationDisplayTimeMultiplier) Toast.Visibility.SHOW else Toast.Visibility.HIDE
    }
}

