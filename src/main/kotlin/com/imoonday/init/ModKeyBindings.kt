package com.imoonday.init

import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.screen.*
import com.imoonday.skill.Skills
import com.imoonday.util.SkillContainer
import com.imoonday.util.isPressedInScreen
import com.imoonday.util.learnableData
import com.imoonday.util.requestUse
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object ModKeyBindings {

    val skillKeys = mutableListOf<KeyBinding>()
    private val pressStates = mutableMapOf<KeyBinding, Boolean>()

    @JvmField
    val OPEN_LIST_SCREEN = register("openListScreen", GLFW.GLFW_KEY_K, false, callback = { client, _ ->
        val player = client.player!!
        client.setScreen(
            if (!player.learnableData.isEmpty() && SkillLearningScreen.new)
                SkillLearningScreen(player) { SkillListScreen(player) }
            else SkillListScreen(player)
        )
    })

    @JvmField
    val OPEN_GALLERY_SCREEN = register("openGalleryScreen", GLFW.GLFW_KEY_G, false, callback = { client, _ ->
        client.setScreen(SkillGalleryScreen().apply {
            selectedSkill = Skills.FIREBALL
        })
    })

    @JvmField
    val OPEN_SLOT_SCREEN = register("openSlotScreen", GLFW.GLFW_KEY_N, false, callback = { client, _ ->
        client.setScreen(SkillSlotScreen())
    })

    @JvmField
    val QUICK_CAST = register("quickCast", GLFW.GLFW_KEY_R, true) { client, _ ->
        client.setScreen(SkillWheelScreen())
    }

    fun init() {
        for (index in 1..SkillContainer.MAX_SLOT_SIZE) registerSkill(
            index,
            if (index <= 6) (GLFW.GLFW_KEY_KP_0 + index) else GLFW.GLFW_KEY_UNKNOWN
        ) { client, keyState ->
            if (client.player?.isSpectator == false) {
                client.player?.requestUse(index, keyState)
            }
        }
    }

    private fun register(
        name: String,
        code: Int,
        longPressCheck: Boolean,
        releaseCallback: (MinecraftClient, KeyBinding) -> Unit = { _, _ -> },
        callback: (MinecraftClient, KeyBinding) -> Unit,
    ): KeyBinding {
        val key = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "advancedSkills.key.$name",
                code,
                "advancedSkills.key.category"
            )
        )
        if (longPressCheck) {
            pressStates[key] = false
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            if (longPressCheck) {
                if (key.isPressed && pressStates[key] == false) {
                    callback(it, key)
                    pressStates[key] = true
                } else if (!key.isPressed && !key.isPressedInScreen && pressStates[key] == true) {
                    releaseCallback(it, key)
                    pressStates[key] = false
                }
            } else {
                if (key.wasPressed()) {
                    callback(it, key)
                } else if (!key.isPressed) {
                    releaseCallback(it, key)
                }
            }
        }
        return key
    }

    private fun registerSkill(
        index: Int,
        code: Int,
        callbacks: (MinecraftClient, UseSkillC2SRequest.KeyState) -> Unit,
    ): KeyBinding {
        val key = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "advancedSkills.key.useSkill$index",
                code,
                "advancedSkills.key.category"
            )
        )
        skillKeys.add(key)
        pressStates[key] = false
        ClientTickEvents.END_CLIENT_TICK.register {
            if (key.isPressed && pressStates[key] == false) {
                callbacks(it, UseSkillC2SRequest.KeyState.PRESS)
                pressStates[key] = true
            } else if (!key.isPressed && pressStates[key] == true) {
                callbacks(it, UseSkillC2SRequest.KeyState.RELEASE)
                pressStates[key] = false
            }
        }
        return key
    }
}