package com.imoonday.util

import com.imoonday.MOD_ID
import com.imoonday.mixin.StatusEffectInstanceAccessor
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.listener.PacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color
import kotlin.math.floor

fun Color.alpha(alpha: Int): Color = Color(this.red, this.green, this.blue, alpha)

fun Color.alpha(multiplier: Double): Color = alpha((this.alpha * multiplier).toInt())

fun PlayerEntity.send(packet: Packet<out PacketListener>) {
    if (this is ServerPlayerEntity) {
        networkHandler.sendPacket(packet)
    } else if (this is ClientPlayerEntity) {
        networkHandler.sendPacket(packet)
    }
}

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

fun PlayerEntity.spawnParticles(
    type: ParticleEffect,
    x: Double,
    y: Double,
    z: Double,
    count: Int,
    deltaX: Double,
    deltaY: Double,
    deltaZ: Double,
    speed: Double,
) {
    (world as? ServerWorld)?.spawnParticles(type, x, y, z, count, deltaX, deltaY, deltaZ, speed)
        ?: world.addParticle(type, x, y, z, deltaX, deltaY, deltaZ)
}

fun ServerPlayerEntity.playSound(sound: SoundEvent) {
    world.playSound(null, blockPos, sound, SoundCategory.PLAYERS)
}

fun id(name: String): Identifier = Identifier(MOD_ID, name)

fun itemId(name: String): Identifier = id("textures/item/$name.png")