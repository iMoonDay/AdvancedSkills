package com.imoonday.util

import com.imoonday.*
import com.imoonday.advanced_skills_re.mixin.*
import dev.architectury.event.*
import net.minecraft.client.*
import net.minecraft.client.option.*
import net.minecraft.client.util.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.util.math.*
import org.lwjgl.glfw.*
import java.awt.*
import kotlin.math.*

fun Color.alpha(alpha: Int): Color = Color(this.red, this.green, this.blue, alpha)

fun Color.alpha(multiplier: Double): Color = alpha((this.alpha * multiplier).toInt())

fun StatusEffectInstance.setDuration(duration: Int) {
    (this as StatusEffectInstanceAccessor).setDuration(duration)
}

val Box.blockPosSet: Set<BlockPos>
    get() {
        val set = mutableSetOf<BlockPos>()
        for (x in floor(minX).toInt()..floor(maxX).toInt()) {
            for (z in floor(minZ).toInt()..floor(maxZ).toInt()) {
                for (y in floor(minY).toInt()..floor(maxY).toInt()) {
                    set.add(BlockPos(x, y, z))
                }
            }
        }
        return set
    }

fun id(name: String): Identifier = Identifier(MOD_ID, name)

fun itemId(name: String): Identifier = id("textures/item/$name.png")

val Box.volume: Double
    get() = this.xLength * this.yLength * this.zLength

fun Vec3d.toBlockPos(): BlockPos = BlockPos.ofFloored(this)

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

fun String.toText(): MutableText = Text.literal(this)

fun String.toIdentifier() = Identifier.tryParse(this)

fun Boolean?.toEventResult(): EventResult = EventResult.interrupt(this)
val Entity.horizontalRotationVector: Vec3d
    get() = getRotationVector(0f, yaw)