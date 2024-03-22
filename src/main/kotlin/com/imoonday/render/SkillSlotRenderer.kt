package com.imoonday.render

import com.imoonday.config.UIConfig
import com.imoonday.init.isSilenced
import com.imoonday.skill.LongPressSkill
import com.imoonday.skill.Skill
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.ProgressTrigger
import com.imoonday.trigger.UsingProgressTrigger
import com.imoonday.util.*
import com.imoonday.util.SkillSlot.Companion.indexTexture
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity
import java.awt.Color
import kotlin.math.PI
import kotlin.math.sin

object SkillSlotRenderer {

    val progressStrings: MutableMap<Int, ScrollingStringRenderer> = mutableMapOf()
    private const val NAME_MAX_WIDTH = 64

    fun render(client: MinecraftClient, context: DrawContext) {
        val player = client.player ?: return
        if (player.isSpectator) return
        val slots = player.skillContainer.getAllSlots()
        val halfSize = if (slots.size <= 5) 5 else slots.size / 2 + if (slots.size % 2 == 0) 0 else 1
        val maxSlotSize = if (slots.size <= 5) slots.size else slots.size / 2 + if (slots.size % 2 == 0) 0 else 1
        slots.forEach {
            if (UIConfig.instance.simplify) {
                val size = 17
                var index = it.index
                if (index > halfSize) index -= halfSize
                val startX =
                    context.scaledWindowWidth - size * (maxSlotSize - index + 1) - UIConfig.instance.uiOffsetX
                val endX = startX + 16
                val startY = if (slots.size > halfSize && it.index <= halfSize) {
                    context.scaledWindowHeight - size * 2 - 5 + UIConfig.instance.uiOffsetY
                } else {
                    context.scaledWindowHeight - size - 1 + UIConfig.instance.uiOffsetY
                }
                val endY = startY + 16
                renderIcon(context, startX, startY, player, it)
                renderIndex(context, startX, startY, it)
                renderProgressBar(context, startX, endY, 16, 1, player, it.skill)
                renderCooldownBar(
                    context,
                    startX,
                    startY,
                    endX,
                    endY,
                    player,
                    it.skill,
                    Color.RED.alpha(0.25).rgb,
                    0,
                    true,
                    16
                )
            } else {
                val startX = context.scaledWindowWidth - 20 - UIConfig.instance.uiOffsetX
                val endX = startX + 16
                val startY =
                    context.scaledWindowHeight / 2 + ((it.index - 1 - slots.size / 2.0) * 18).toInt() + UIConfig.instance.uiOffsetY
                val endY = startY + 16
                renderIcon(context, startX, startY, player, it)
                renderIndex(context, endX, startY, it)
                renderName(context, client, startX, startY, it)
                renderProgressBar(context, startX - 34, endY - 2, 32, 1, player, it.skill)
                renderCooldownBar(
                    context,
                    startX - 34,
                    endY,
                    startX - 2,
                    endY + 1,
                    player,
                    it.skill,
                    Color.RED.rgb,
                    Color.GRAY.rgb,
                    false,
                    32
                )
            }
        }
    }

    private fun renderIndex(
        context: DrawContext,
        endX: Int,
        startY: Int,
        slot: SkillSlot,
    ) {
        val stack = context.matrices
        stack.push()
        val scale = 0.75f
        stack.scale(scale, scale, scale)
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.75f)
        context.drawTexture(
            indexTexture,
            ((endX - 3) / scale).toInt(),
            ((startY - 3) / scale).toInt(),
            slot.u,
            slot.v,
            9,
            9
        )
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.disableBlend()
        stack.pop()
    }

    private fun renderProgressBar(
        context: DrawContext,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        player: ClientPlayerEntity,
        skill: Skill,
    ) {
        if (skill.invalid) return
        if (skill is ProgressTrigger && skill.shouldDisplay(player)
            && (player.isUsing(skill) || skill !is UsingProgressTrigger)
        ) {
            val progress = skill.getProgress(player)
            val centerX = startX + (width * progress).toInt()
            context.fill(startX, startY, centerX, startY + height, Color.GREEN.rgb)
            context.fill(centerX, startY, startX + width, startY + height, Color.GRAY.rgb)
        }
    }

    private fun renderCooldownBar(
        context: DrawContext,
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        player: ClientPlayerEntity,
        skill: Skill,
        color: Int,
        backgroundColor: Int,
        vertical: Boolean,
        length: Int,
    ) {
        if (!player.isCooling(skill)) return
        val cooldown = player.getCooldown(skill)
        val maxCooldown = skill.getCooldown(player.world)
        val progress = ((cooldown.toDouble() / maxCooldown).coerceIn(0.0, 1.0) * length).toInt()
        if (vertical) {
            val centerY = endY - progress
            context.fill(startX, centerY, endX, endY, color)
            context.fill(startX, startY, endX, centerY, backgroundColor)
        } else {
            val centerX = startX + progress
            context.fill(startX, startY, centerX, endY, color)
            context.fill(centerX, startY, endX, endY, backgroundColor)
        }
    }

    private fun renderName(
        context: DrawContext,
        client: MinecraftClient,
        startX: Int,
        startY: Int,
        slot: SkillSlot,
    ) {
        val index = slot.index
        val skill = slot.skill
        if (skill.invalid) return
        val text = skill.name.string
        if (progressStrings[index]?.text?.string != text) {
            progressStrings[index] = ScrollingStringRenderer(text, NAME_MAX_WIDTH, client.textRenderer)
        }
        progressStrings[index]?.render(context, startX - NAME_MAX_WIDTH - 2, startY)
    }

    private fun renderIcon(
        context: DrawContext,
        startX: Int,
        startY: Int,
        player: ClientPlayerEntity,
        slot: SkillSlot,
    ) {
        val skill = slot.skill
        if (skill is AutoStopTrigger && skill !is LongPressSkill) {
            val persistTime = skill.getPersistTime()
            val leftUseTime = persistTime - player.getUsedTime(skill)
            if (persistTime > 20 * 5 && leftUseTime <= persistTime / 5) {
                val alpha = 0.5 * sin(2 * PI / 20 * (leftUseTime - persistTime / 5)) + 0.5
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha.toFloat())
            }
        }
        context.drawTexture(
            if (player.isSilenced) Skill.EMPTY.icon else skill.icon,
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