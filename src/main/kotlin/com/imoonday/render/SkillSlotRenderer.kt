package com.imoonday.render

import com.imoonday.component.*
import com.imoonday.config.UIConfigModel
import com.imoonday.init.ModSkills
import com.imoonday.init.isSilenced
import com.imoonday.skill.LongPressSkill
import com.imoonday.skill.Skill
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.UsingProgressTrigger
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity
import java.awt.Color
import kotlin.math.PI
import kotlin.math.sin

object SkillSlotRenderer {

    val progressStrings: MutableList<ScrollingStringRenderer?> = mutableListOf(null, null, null, null)

    fun render(client: MinecraftClient, context: DrawContext) {
        if (client.player?.isSpectator == false) {
            client.player?.equippedSkills?.forEachIndexed { index, skill ->
                val player = client.player!!
                val startX = context.scaledWindowWidth - 32 - UIConfigModel.instance.uiOffsetX
                val endX = startX + 16
                val startY = context.scaledWindowHeight / 2 + (index - 2) * 18 + UIConfigModel.instance.uiOffsetY
                val endY = startY + 16
                renderIndex(context, client, index, endX, startY)
                renderIcon(skill, player, context, startX, startY)
                renderName(context, client, index, skill, startX, startY)
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
        if (player.isUsingSkill(skill) && skill is UsingProgressTrigger && skill.shouldDisplay(player)) {
            val progress = skill.getProgress(player)
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
        index: Int,
        skill: Skill,
        startX: Int,
        startY: Int,
    ) {
        if (index !in 0..3) return
        val text = skill.name.string
        if (progressStrings[index] == null || progressStrings[index]!!.text.string != text) {
            progressStrings[index] = ScrollingStringRenderer(text, 64, client.textRenderer)
        }
        progressStrings[index]!!.render(context, startX - 64 - 2, startY)
    }

    private fun renderIcon(
        skill: Skill,
        player: ClientPlayerEntity,
        context: DrawContext,
        startX: Int,
        startY: Int,
    ) {
        if (skill is AutoStopTrigger && skill !is LongPressSkill) {
            val leftUseTime = skill.getPersistTime() - player.getSkillUsedTime(skill)
            if (skill.getPersistTime() > 20 * 5 && leftUseTime <= skill.getPersistTime() / 5) {
                val alpha =
                    (0.5 * sin((2 * PI / 20) * (leftUseTime - skill.getPersistTime() / 5)) + 0.5).toFloat()
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha)
            }
        }
        context.drawTexture(
            if (player.isSilenced) ModSkills.EMPTY.icon else skill.icon,
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