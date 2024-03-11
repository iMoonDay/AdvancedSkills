package com.imoonday.render

import com.imoonday.component.*
import com.imoonday.config.UIConfigModel
import com.imoonday.init.ModSkills
import com.imoonday.init.isSilenced
import com.imoonday.skill.LongPressSkill
import com.imoonday.skill.Skill
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.UsingProgressTrigger
import com.imoonday.util.id
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity
import java.awt.Color
import kotlin.math.PI
import kotlin.math.sin

object SkillSlotRenderer {

    val progressStrings: MutableList<ScrollingStringRenderer?> = mutableListOf(null, null, null, null)
    private const val NAME_MAX_WIDTH = 64

    @JvmStatic
    val indexTexture = id("index.png")

    fun render(client: MinecraftClient, context: DrawContext) {
        val player = client.player ?: return
        if (player.isSpectator) return
        player.equippedSkills.forEachIndexed { index, skill ->
            val startX = context.scaledWindowWidth - 20 - UIConfigModel.instance.uiOffsetX
            val endX = startX + 16
            val startY = context.scaledWindowHeight / 2 + (index - 2) * 18 + UIConfigModel.instance.uiOffsetY
            val endY = startY + 16
            renderIcon(context, startX, startY, player, skill)
            renderIndex(context, index, endX, startY)
            renderName(context, client, startX, startY, index, skill)
            renderProgressBar(context, startX, endY, player, skill)
        }
    }

    private fun renderIndex(
        context: DrawContext,
        index: Int,
        endX: Int,
        startY: Int,
    ) {
        val stack = context.matrices
        stack.push()
        val scale = 0.75f
        stack.scale(scale, scale, scale)
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.75f)
        context.drawTexture(indexTexture, ((endX - 3) / scale).toInt(), ((startY - 3) / scale).toInt(), index * 9, 0, 9, 9)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.disableBlend()
        stack.pop()
    }

    private fun renderProgressBar(
        context: DrawContext,
        startX: Int,
        endY: Int,
        player: ClientPlayerEntity,
        skill: Skill,
    ) {
        if (skill.invalid) return
        val lineEndX = startX - 2
        val lineStartX = startX - 20 - 14
        if (player.isUsingSkill(skill) && skill is UsingProgressTrigger && skill.shouldDisplay(player)) {
            val progress = skill.getProgress(player)
            val centerX = lineStartX + (32 * progress).toInt()
            context.fill(lineStartX, endY - 2, centerX, endY - 1, Color.GREEN.rgb)
            context.fill(centerX, endY - 2, lineEndX, endY - 1, Color.GRAY.rgb)
        }
        if (player.isCooling(skill)) {
            val cooldown = player.getCooldown(skill)
            val maxCooldown = skill.getCooldown(player.world)
            val progress = ((cooldown.toDouble() / maxCooldown).coerceIn(0.0, 1.0) * 32).toInt()
            val centerX = lineStartX + progress
            context.fill(lineStartX, endY, centerX, endY + 1, Color.RED.rgb)
            context.fill(centerX, endY, lineEndX, endY + 1, Color.GRAY.rgb)
        }
    }

    private fun renderName(
        context: DrawContext,
        client: MinecraftClient,
        startX: Int,
        startY: Int,
        index: Int,
        skill: Skill,
    ) {
        if (skill.invalid) return
        if (index !in 0..3) return
        val text = skill.name.string
        if (progressStrings[index] == null || progressStrings[index]!!.text.string != text) {
            progressStrings[index] = ScrollingStringRenderer(text, NAME_MAX_WIDTH, client.textRenderer)
        }
        progressStrings[index]!!.render(context, startX - NAME_MAX_WIDTH - 2, startY)
    }

    private fun renderIcon(
        context: DrawContext,
        startX: Int,
        startY: Int,
        player: ClientPlayerEntity,
        skill: Skill,
    ) {
        if (skill is AutoStopTrigger && skill !is LongPressSkill) {
            val persistTime = skill.getPersistTime()
            val leftUseTime = persistTime - player.getSkillUsedTime(skill)
            if (persistTime > 20 * 5 && leftUseTime <= persistTime / 5) {
                val alpha = 0.5 * sin(2 * PI / 20 * (leftUseTime - persistTime / 5)) + 0.5
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha.toFloat())
            }
        }
        context.drawTexture(if (player.isSilenced) ModSkills.EMPTY.icon else skill.icon, startX, startY, 0f, 0f, 16, 16, 16, 16)
        if (skill is AutoStopTrigger && skill !is LongPressSkill) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.disableBlend()
        }
    }
}