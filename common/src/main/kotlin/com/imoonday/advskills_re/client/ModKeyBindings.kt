package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import dev.architectury.event.events.client.*
import dev.architectury.registry.client.keymappings.*
import net.fabricmc.api.*
import net.minecraft.client.*
import net.minecraft.client.option.*
import org.lwjgl.glfw.*

@Environment(EnvType.CLIENT)
object ModKeyBindings {

    val skillKeys = mutableListOf<KeyBinding>()
    private var isUsingQuickCast = false

    @JvmField
    val OPEN_LIST_SCREEN = register("openListScreen", GLFW.GLFW_KEY_K, false) { client, _ ->
        val player = client.player!!
        val listScreen = SkillListScreen(player)
        client.setScreen(
            if (!player.learnableData.isEmpty() && SkillLearningScreen.new)
                SkillLearningScreen(player) { listScreen }
            else listScreen
        )
    }

    @JvmField
    val OPEN_GALLERY_SCREEN = register("openGalleryScreen", GLFW.GLFW_KEY_G, false) { client, _ ->
        client.setScreen(SkillGalleryScreen(Skills.FIREBALL))
    }

    @JvmField
    val OPEN_SLOT_SCREEN = register("openSlotScreen", GLFW.GLFW_KEY_N, false) { client, _ ->
        client.setScreen(SkillSlotScreen())
    }

    @JvmField
    val QUICK_CAST = registerWithDoubleTrigger(
        "quickCast",
        GLFW.GLFW_KEY_R,
        { ClientConfig.instance.quickCastWheelHoldTime },
        firstTriggerCallback = { client, _ ->
            val slot = SkillWheelScreen.quickCastSlot
            slot != null && client.player?.getSkill(slot)?.isEmpty() != true
        },
        secondTriggerCallback = { client, _ ->
            if (!isUsingQuickCast) {
                isUsingQuickCast = true
                client.setScreen(SkillWheelScreen())
            }
        },
        releaseCallback = { client, _, pressTime ->
            isUsingQuickCast = false
            if (pressTime <= 250) {
                client.player?.run {
                    val slot = SkillWheelScreen.quickCastSlot ?: return@run
                    if (!isSpectator) {
                        requestUse(
                            slot,
                            if (isCharging(getSkill(slot))) UseSkillC2SRequest.KeyState.RELEASE
                            else UseSkillC2SRequest.KeyState.PRESS
                        )
                    }
                }
            }
        }
    )

    fun init() {
        for (index in 1..SkillContainer.MAX_SLOT_SIZE) {
            registerSkill(
                index,
                if (index <= 6) (GLFW.GLFW_KEY_KP_0 + index)
                else GLFW.GLFW_KEY_UNKNOWN
            ) { client, keyState -> client.player?.run { if (!isSpectator) requestUse(index, keyState) } }
        }
    }

    private fun register(
        name: String,
        code: Int,
        longPressCheck: Boolean,
        releaseCallback: (MinecraftClient, KeyBinding) -> Unit = { _, _ -> },
        callback: (MinecraftClient, KeyBinding) -> Unit,
    ): KeyBinding {
        val key = KeyBinding(
            "advskills_re.key.$name",
            code,
            "advskills_re.key.category"
        )
        KeyMappingRegistry.register(key)
        // 按键状态变量
        var isPressed = false

        ClientTickEvent.CLIENT_POST.register { client ->
            if (longPressCheck) {
                if (key.isPressed) {
                    if (!isPressed) {
                        callback(client, key) // 按下触发
                        isPressed = true
                    }
                } else {
                    if (isPressed) {
                        releaseCallback(client, key) // 松开触发
                        isPressed = false
                    }
                }
            } else {
                if (key.wasPressed()) {
                    callback(client, key) // 按下瞬间触发
                } else if (!key.isPressed) {
                    releaseCallback(client, key) // 松开触发
                }
            }
        }

        return key
    }

    private fun registerWithDoubleTrigger(
        name: String,
        code: Int,
        interval: () -> Int, // 二次触发的间隔时间（毫秒）
        firstTriggerCallback: (MinecraftClient, KeyBinding) -> Boolean,
        secondTriggerCallback: (MinecraftClient, KeyBinding) -> Unit,
        releaseCallback: (MinecraftClient, KeyBinding, pressTime: Long) -> Unit,
    ): KeyBinding {
        val key = KeyBinding(
            "advskills_re.key.$name",
            code,
            "advskills_re.key.category"
        )
        KeyMappingRegistry.register(key)
        // 用于记录按键状态和计时
        var isPressed = false
        var secondTriggered = false
        var pressStartTime: Long = 0

        ClientTickEvent.CLIENT_POST.register { client ->
            if (key.isPressed || isPressed && key.isPressedInScreen) {
                if (!isPressed) {
                    // 第一次触发
                    val firstTriggerResult = firstTriggerCallback(client, key)
                    isPressed = true

                    pressStartTime = if (!firstTriggerResult) {
                        // 如果第一次触发返回 false，则立即执行二次触发逻辑
                        secondTriggerCallback(client, key)
                        secondTriggered = true
                        System.currentTimeMillis() - interval()
                    } else {
                        secondTriggered = false
                        System.currentTimeMillis()
                    }
                } else if (!secondTriggered && System.currentTimeMillis() - pressStartTime >= interval()) {
                    // 二次触发
                    secondTriggerCallback(client, key)
                    secondTriggered = true
                }
            } else if (isPressed) {
                // 松开时触发
                releaseCallback(client, key, System.currentTimeMillis() - pressStartTime)
                isPressed = false
            }
        }

        return key
    }

    private fun registerSkill(
        index: Int,
        code: Int,
        callbacks: (MinecraftClient, UseSkillC2SRequest.KeyState) -> Unit,
    ): KeyBinding {
        val key = KeyBinding(
            "advskills_re.key.useSkill$index",
            code,
            "advskills_re.key.category"
        )
        KeyMappingRegistry.register(key)
        skillKeys.add(key)
        // 按键状态变量
        var isPressed = false

        ClientTickEvent.CLIENT_POST.register { client ->
            if (key.isPressed) {
                if (!isPressed) {
                    // 按下触发
                    callbacks(client, UseSkillC2SRequest.KeyState.PRESS)
                    isPressed = true
                }
            } else {
                if (isPressed) {
                    // 松开触发
                    callbacks(client, UseSkillC2SRequest.KeyState.RELEASE)
                    isPressed = false
                }
            }
        }

        return key
    }
}