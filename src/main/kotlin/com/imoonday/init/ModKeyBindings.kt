package com.imoonday.init

import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.screen.SkillGalleryScreen
import com.imoonday.screen.SkillListScreen
import com.imoonday.screen.SkillSlotScreen
import com.imoonday.screen.SkillWheelScreen
import com.imoonday.skill.Skills
import com.imoonday.util.SkillContainer.Companion.MAX_SLOT_SIZE
import com.imoonday.util.requestUse
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object ModKeyBindings {

    @JvmField
    val OPEN_LIST_SCREEN = register("openListScreen", GLFW.GLFW_KEY_K, { client, _ ->
        client.setScreen(SkillListScreen(client.player!!))
    })

    @JvmField
    val OPEN_GALLERY_SCREEN = register("openGalleryScreen", GLFW.GLFW_KEY_G, { client, _ ->
        client.setScreen(SkillGalleryScreen().apply {
            selectedSkill = Skills.FIREBALL
        })
    })

    @JvmField
    val OPEN_SLOT_SCREEN = register("openSlotScreen", GLFW.GLFW_KEY_N, { client, _ ->
        client.setScreen(SkillSlotScreen())
    })

    @JvmField
    val QUICK_CAST = register("quickCast", GLFW.GLFW_KEY_R, { client, key ->
        if (pressStates[key] != true) {
            pressStates[key] = true
            client.setScreen(SkillWheelScreen())
        }
    }) { client, key ->
        if (pressStates[key] == true && client.currentScreen == null) {
            pressStates.remove(key)
        }
    }

    fun init() {
        for (index in 1..MAX_SLOT_SIZE) registerSkill(if (index <= 6) (GLFW.GLFW_KEY_KP_0 + index) else GLFW.GLFW_KEY_UNKNOWN) { client, keyState ->
            if (client.player?.isSpectator == false) {
                client.player?.requestUse(index, keyState)
            }
        }
    }

    private val pressStates = mutableMapOf<KeyBinding, Boolean>()
    private var usingQuickCast = false

    private fun register(
        name: String,
        code: Int,
        callback: (MinecraftClient, KeyBinding) -> Unit,
        releaseCallback: (MinecraftClient, KeyBinding) -> Unit = { _, _ -> },
    ): KeyBinding {
        val key = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "advancedSkills.key.$name",
                code,
                "advancedSkills.key.category"
            )
        )
        ClientTickEvents.END_CLIENT_TICK.register {
            if (key.wasPressed()) {
                callback.invoke(it, key)
            } else if (!key.isPressed) {
                releaseCallback.invoke(it, key)
            }
        }
        return key
    }

    private fun registerSkill(
        code: Int,
        callbacks: (MinecraftClient, UseSkillC2SRequest.KeyState) -> Unit,
    ): KeyBinding {
        val key = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "advancedSkills.key.useSkill${pressStates.size + 1}",
                code,
                "advancedSkills.key.category"
            )
        )
        pressStates[key] = false
        ClientTickEvents.END_CLIENT_TICK.register {
            if (key.isPressed && pressStates[key] == false) {
                callbacks.invoke(it, UseSkillC2SRequest.KeyState.PRESS)
                pressStates[key] = true
            } else if (!key.isPressed && pressStates[key] == true) {
                callbacks.invoke(it, UseSkillC2SRequest.KeyState.RELEASE)
                pressStates[key] = false
            }
        }
        return key
    }
}