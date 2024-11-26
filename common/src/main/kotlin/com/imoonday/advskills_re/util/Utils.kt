package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.mixin.*
import dev.architectury.event.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.util.math.*
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

fun String.toText(): MutableText = Text.literal(this)

fun String.toIdentifier() = Identifier.tryParse(this)

fun Boolean?.toEventResult(): EventResult = EventResult.interrupt(this)

val Entity.horizontalRotationVector: Vec3d
    get() = getRotationVector(0f, yaw)