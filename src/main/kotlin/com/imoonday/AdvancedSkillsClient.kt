package com.imoonday

import com.imoonday.components.*
import com.imoonday.config.UIConfig
import com.imoonday.init.ModEntities
import com.imoonday.init.ModKeyBindings
import com.imoonday.init.isSilenced
import com.imoonday.network.Channels
import com.imoonday.skills.LongPressSkill
import com.imoonday.skills.Skills
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.PersistentTrigger
import com.imoonday.utils.alpha
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import java.awt.Color

object AdvancedSkillsClient : ClientModInitializer {
    override fun onInitializeClient() {
        UIConfig.load()
        ModKeyBindings.init()
        Channels.registerClient()
        ModEntities.initClient()
        registerEvents()
    }

    private fun registerEvents() {
        HudRenderCallback.EVENT.register { context, _ ->
            val client = MinecraftClient.getInstance()
            if (client.currentScreen == null && client.player?.isSpectator == false) {
                client.player?.equippedSkills?.forEachIndexed { index, skill ->
                    val player = client.player!!
                    if (UIConfig.instance.newStyle) {
                        val startX =
                            context.scaledWindowWidth - 32
                        val endX = startX + 16
                        val startY = context.scaledWindowHeight / 2 + (index - 2) * 18
                        val endY = startY + 16
                        context.drawCenteredTextWithShadow(
                            client.textRenderer,
                            (index + 1).toString(),
                            endX + 8,
                            startY + client.textRenderer.fontHeight / 2,
                            Color.WHITE.rgb
                        )
                        context.drawTexture(
                            if (player.isSilenced()) Skills.EMPTY.icon else skill.icon,
                            startX,
                            startY,
                            0f,
                            0f,
                            16,
                            16, 16, 16
                        )
                        context.drawTextWithShadow(
                            client.textRenderer,
                            skill.name.string,
                            startX - client.textRenderer.getWidth(skill.name.string) - 2,
                            startY + client.textRenderer.fontHeight / 2,
                            Color.WHITE.rgb
                        )
                        val lineEndX = startX - 2
                        val lineStartX = startX - 20 - 14
                        if (player.isUsingSkill(skill) && (skill is AutoStopTrigger || skill is PersistentTrigger && skill.isActive(
                                player
                            ))
                        ) {
                            var progress =
                                if (skill is AutoStopTrigger) (skill.persistTime - player.getSkillUsedTime(skill)) / skill.persistTime.toDouble() else 1.0
                            if (skill is LongPressSkill) progress = 1 - progress
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
                            val progress = ((player.getCooldown(skill).toDouble() / skill.cooldown) * 32).toInt()
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
                    } else {
                        val startX =
                            (context.scaledWindowWidth * UIConfig.instance.slotXScaling - 16 * (4 - index)).toInt()
                        val endX = startX + 16
                        val startY = (context.scaledWindowHeight * UIConfig.instance.slotYScaling - 16).toInt()
                        val endY = startY + 16
                        context.drawTexture(
                            skill.icon,
                            startX,
                            startY,
                            0f,
                            0f,
                            16,
                            16, 16, 16
                        )
                        if (player.isSilenced()) {
                            context.fill(
                                startX,
                                startY,
                                endX,
                                endY,
                                Color.WHITE.alpha(0.5).rgb
                            )
                        } else if (player.isCooling(skill)) {
                            val height = ((player.getCooldown(skill).toDouble() / skill.cooldown) * 16).toInt()
                            context.fill(
                                startX,
                                endY - height,
                                endX,
                                endY,
                                Color.RED.alpha(0.5).rgb
                            )
                        }
                        if (player.isUsingSkill(skill) && (skill is AutoStopTrigger || skill is PersistentTrigger && skill.isActive(
                                player
                            ))
                        ) {
                            val progress =
                                if (skill is AutoStopTrigger) (skill.persistTime - player.getSkillUsedTime(skill)) / skill.persistTime.toDouble() else 1.0
                            context.fill(
                                startX,
                                startY - 3,
                                startX + (16 * progress).toInt(),
                                startY - 1,
                                Color.GREEN.rgb
                            )
                        }
                    }
                }
            }
        }
    }
}