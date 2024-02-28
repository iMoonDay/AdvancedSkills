package com.imoonday.render

import com.imoonday.components.*
import com.imoonday.config.Config
import com.imoonday.init.isSilenced
import com.imoonday.skills.LongPressSkill
import com.imoonday.skills.Skills
import com.imoonday.triggers.AutoStopTrigger
import com.imoonday.triggers.PersistentTrigger
import com.imoonday.utils.Skill
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity
import java.awt.Color
import kotlin.math.PI
import kotlin.math.sin

object SkillSlotRenderer {

    fun render(client: MinecraftClient, context: DrawContext, delta: Float) {
        if (client.player?.isSpectator == false) {
            client.player?.equippedSkills?.forEachIndexed { index, skill ->
                val player = client.player!!
                val startX = context.scaledWindowWidth - 32 - Config.instance.offsetX
                val endX = startX + 16
                val startY = context.scaledWindowHeight / 2 + (index - 2) * 18 + Config.instance.offsetY
                val endY = startY + 16
                renderIndex(context, client, index, endX, startY)
                renderIcon(skill, player, context, startX, startY)
                renderName(context, client, skill, startX, startY)
                renderProgressBar(startX, player, skill, context, endY)
            }
        }
    }

    private fun renderIndex(
        context: DrawContext,
        client: MinecraftClient,
        index: Int,
        endX: Int,
        startY: Int,
    ) {
        context.drawCenteredTextWithShadow(
            client.textRenderer,
            (index + 1).toString(),
            endX + 8,
            startY + client.textRenderer.fontHeight / 2,
            Color.WHITE.rgb
        )
    }

    private fun renderProgressBar(
        startX: Int,
        player: ClientPlayerEntity,
        skill: Skill,
        context: DrawContext,
        endY: Int,
    ) {
        val lineEndX = startX - 2
        val lineStartX = startX - 20 - 14
        if (player.isUsingSkill(skill) && (skill is AutoStopTrigger || skill is PersistentTrigger && skill.isActive(
                player
            ))
        ) {
            var progress =
                (if (skill is AutoStopTrigger) (skill.persistTime - player.getSkillUsedTime(skill)) / skill.persistTime.toDouble() else 1.0).coerceIn(
                    0.0,
                    1.0
                )
            if (skill is LongPressSkill) progress = 1.0 - progress
            val centerX = lineStartX + (32 * progress).toInt()
            context.fill(
                lineStartX,
                endY - 2,
                centerX,
                endY - 1,
                Color.GREEN.rgb
            )
            context.fill(
                centerX,
                endY - 2,
                lineEndX,
                endY - 1,
                Color.GRAY.rgb
            )
        }
        if (player.isCooling(skill)) {
            val progress = ((player.getCooldown(skill).toDouble() / skill.cooldown).coerceIn(0.0, 1.0) * 32).toInt()
            val centerX = lineStartX + progress
            context.fill(
                lineStartX,
                endY,
                centerX,
                endY + 1,
                Color.RED.rgb
            )
            context.fill(
                centerX,
                endY,
                lineEndX,
                endY + 1,
                Color.GRAY.rgb
            )
        }
    }

    private fun renderName(
        context: DrawContext,
        client: MinecraftClient,
        skill: Skill,
        startX: Int,
        startY: Int,
    ) {
        context.drawTextWithShadow(
            client.textRenderer,
            skill.name.string,
            startX - client.textRenderer.getWidth(skill.name.string) - 2,
            startY + client.textRenderer.fontHeight / 2,
            Color.WHITE.rgb
        )
    }

    private fun renderIcon(
        skill: Skill,
        player: ClientPlayerEntity,
        context: DrawContext,
        startX: Int,
        startY: Int,
    ) {
        if (skill is AutoStopTrigger && skill !is LongPressSkill) {
            val leftUseTime = skill.persistTime - player.getSkillUsedTime(skill)
            if (skill.persistTime > 20 * 5 && leftUseTime <= skill.persistTime / 5) {
                val alpha =
                    (0.5 * sin((2 * PI / 20) * (leftUseTime - skill.persistTime / 5)) + 0.5).toFloat()
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha)
            }
        }
        context.drawTexture(
            if (player.isSilenced) Skills.EMPTY.icon else skill.icon,
            startX,
            startY,
            0f,
            0f,
            16,
            16,
            16,
            16
        )
        if (skill is AutoStopTrigger && skill !is LongPressSkill) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.disableBlend()
        }
    }
}