package com.imoonday.init

import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.screen.SkillGalleryScreen
import com.imoonday.screen.SkillListScreen
import com.imoonday.screen.SkillSlotScreen
import com.imoonday.trigger.SendPlayerDataTrigger
import com.imoonday.trigger.SendTime
import com.imoonday.util.SkillSlot
import com.imoonday.util.getSkill
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.nbt.NbtCompound
import org.lwjgl.glfw.GLFW

object ModKeyBindings {

    @JvmField
    val OPEN_LIST_SCREEN = register("openListScreen", GLFW.GLFW_KEY_K) { client ->
        client.setScreen(SkillListScreen(client.player!!))
    }

    @JvmField
    val OPEN_GALLERY_SCREEN = register("openGalleryScreen", GLFW.GLFW_KEY_G) {
        it.setScreen(SkillGalleryScreen().apply {
            selectedSkill = ModSkills.FIREBALL
        })
    }

    @JvmField
    val OPEN_SLOT_SCREEN = register("openSlotScreen", GLFW.GLFW_KEY_N) {
        it.setScreen(SkillSlotScreen())
    }

    fun init() {
        for (i in 1..4) registerSkill(GLFW.GLFW_KEY_KP_0 + i) { client, keyState ->
            if (client.player?.isSpectator == false) {
                val slot = SkillSlot.fromIndex(i)
                ClientPlayNetworking.send(
                    UseSkillC2SRequest(
                        slot,
                        keyState,
                        NbtCompound().apply {
                            (client.player!!.getSkill(slot) as? SendPlayerDataTrigger)
                                ?.takeIf { it.getSendTime() == SendTime.USE }
                                ?.write(client.player!!, this)
                        }
                    )
                )
            }
        }
    }

    val pressStates = mutableMapOf<KeyBinding, Boolean>()

    private fun register(name: String, code: Int, callbacks: (MinecraftClient) -> Unit): KeyBinding {
        val key = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "advancedSkills.key.$name",
                code,
                "advancedSkills.key.category"
            )
        )
        ClientTickEvents.END_CLIENT_TICK.register {
            if (key.wasPressed()) callbacks.invoke(it)
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