package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.trigger.*
import net.minecraft.client.*
import net.minecraft.client.network.*
import net.minecraft.client.option.*
import net.minecraft.client.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.network.listener.*
import net.minecraft.network.packet.*
import org.lwjgl.glfw.*

object ClientUtils {

    fun sendToServer(packet: Packet<out PacketListener>) {
        client?.networkHandler?.sendPacket(packet)
    }
}

val client: MinecraftClient?
    get() = MinecraftClient.getInstance()

val KeyBinding.isPressedInScreen: Boolean
    get() = client?.window?.let {
        keyCategory == InputUtil.Type.KEYSYM
            && InputUtil.isKeyPressed(it.handle, keyCode)
            || keyCategory == InputUtil.Type.MOUSE
            && GLFW.glfwGetMouseButton(it.handle, keyCode) == GLFW.GLFW_PRESS
    } ?: false

val KeyBinding.key: InputUtil.Key
    get() = (this as KeyBindingAccessor).boundKey

val KeyBinding.keyCode: Int
    get() = key.code

val KeyBinding.keyCategory: InputUtil.Type
    get() = key.category

fun PlayerEntity.updateScreen() {
    if (world.isClient) {
        val screen = client!!.currentScreen
        if (screen is AutoSyncedScreen && (screen as ScreenAccessor).isScreenInitialized) {
            screen.update()
        }
    }
}

val clientPlayer: ClientPlayerEntity?
    get() = client?.player

fun ClientPlayerEntity.requestUse(
    index: Int,
    keyState: UseSkillC2SRequest.KeyState,
) {
    Channels.USE_SKILL_C2S.sendToServer(
        UseSkillC2SRequest(
            index,
            keyState,
            NbtCompound().apply {
                (getSkill(index) as? SendPlayerDataTrigger)
                    ?.takeIf { it.getSendTime() == SendTime.USE }
                    ?.write(this@requestUse, this)
            }
        )
    )
}