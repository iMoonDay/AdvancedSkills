package com.imoonday.advskills_re.client.render.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.gui.tooltip.*
import net.minecraft.entity.player.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.awt.*
import kotlin.math.*

object SkillRenderer {

    @JvmStatic
    fun render(
        skill: Skill,
        context: DrawContext,
        x: Int,
        y: Int,
        player: PlayerEntity,
        progressBarOffsetY: Int = 0,
        noProgressBar: Boolean = false,
    ) {
        val endY = y + 16
        renderIcon(skill, context, x, y, player)
        if (!noProgressBar) {
            renderProgressBar(skill, context, x, endY - 1 + progressBarOffsetY, 16, 1, player)
        }
        renderCooldownOverlay(skill, context, x, endY, 16, 16, player)
    }

    @JvmStatic
    fun renderIcon(
        skill: Skill,
        context: DrawContext,
        x: Int,
        y: Int,
        player: PlayerEntity?
    ) {
        var flashed = false
        if (player != null && skill is AutoStopTrigger && skill.shouldFlashIcon(player)) {
            flashed = true
            val persistTime = skill.getModifiedPersistTime(player)
            val leftUseTime = persistTime - player.getUsedTime(skill)
            if (persistTime > 5 * 20 && leftUseTime < (persistTime / 5).coerceAtMost(10 * 20)) {
                val alpha = 0.5 * sin(2 * PI / 20 * (leftUseTime - persistTime / 5)) + 0.5
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha.toFloat())
            }
        }
        if (!skill.isEmpty()) renderIcon(skill, context, x, y)
        if (flashed) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.disableBlend()
        }
        if (player?.isSilenced == true) context.fill(x, y, x + 16, y + 16, Color.RED.alpha(0.25).rgb)
    }

    @JvmStatic
    fun renderIcon(
        skill: Skill,
        context: DrawContext,
        x: Int,
        y: Int,
        size: Int = 16,
    ) = context.drawTexture(skill.icon, x, y, 0f, 0f, size, size, size, size)

    @JvmStatic
    fun renderProgressBar(
        skill: Skill,
        context: DrawContext,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        player: PlayerEntity,
    ) {
        if (skill.invalid || !player.hasLearned(skill)) return
        if (skill is ProgressTrigger && skill.shouldDisplay(player)
            && (player.isUsing(skill) || skill !is UsingProgressTrigger)
        ) {
            val progress = skill.getProgress(player).coerceIn(0.0, 1.0)
            val centerX = x + 1 + ((width - 1) * progress).toInt()
            context.fill(x, y, centerX, y + height, ClientConfig.get().progressBarColor)
            context.fill(centerX, y, x + width, y + height, Color.GRAY.rgb)
        }
    }

    @JvmStatic
    fun renderCooldownOverlay(
        skill: Skill,
        context: DrawContext,
        startX: Int,
        endY: Int,
        width: Int,
        maxHeight: Int,
        player: PlayerEntity,
    ) {
        if (!player.isCooling(skill)) return
        val cooldown = player.getCooldown(skill)
        val maxCooldown = skill.cooldown
        val progress = (cooldown.toDouble() / maxCooldown).coerceIn(0.0, 1.0)
        val startY = (endY - progress * maxHeight).toInt()

        val overlayColor = Color.BLACK.alpha(0.25).rgb
        context.fill(startX, endY - maxHeight, startX + width, endY, overlayColor)
        context.fill(startX, startY, startX + width, endY, overlayColor)
        if (cooldown < 4 * 20) {
            val time = if (cooldown <= 20) String.format("%.1f", cooldown / 20.0) else (cooldown / 20).toString()
            val textRenderer = client!!.textRenderer
            context.matrices.push()
            context.matrices.translate(
                startX + (width - textRenderer.getWidth(time)) / 2.0 + 0.5,
                endY - width / 2.0 + 1,
                0.0
            )
            context.drawText(textRenderer, time, 0, 0, 0xFFFFFF, false)
            context.matrices.pop()
        }
    }

    @JvmStatic
    fun renderTooltip(
        client: MinecraftClient,
        skill: Skill,
        context: DrawContext,
        x: Int,
        y: Int,
        player: PlayerEntity,
    ) {
        val orderedList = getTooltip(client, skill, player)
        context.drawOrderedTooltip(client.textRenderer, orderedList, x, y)
    }

    @JvmStatic
    fun getTooltip(
        client: MinecraftClient,
        skill: Skill,
        player: PlayerEntity
    ): MutableList<OrderedText> {
        val list = skill.getItemTooltips(displayName = true)
        val tooltips = skill.getEnhancementTooltips(player)
        if (tooltips.isNotEmpty()) {
            list.add(Text.empty())
            list.add(translate("screen.inventory.enhance").formatted(Formatting.GRAY))
            list.addAll(tooltips)
        }
        list.add(skill.id.toString().toText().formatted(Formatting.DARK_GRAY))
        val orderedList = list.map(Text::asOrderedText).toMutableList()
        val lines = Tooltip.wrapLines(client, list[1])
        if (lines.size > 1) {
            orderedList.removeAt(1)
            orderedList.addAll(1, lines)
        }
        return orderedList
    }
}