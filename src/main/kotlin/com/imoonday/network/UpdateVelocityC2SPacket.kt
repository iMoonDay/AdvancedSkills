package com.imoonday.network

import com.imoonday.util.id
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

class UpdateVelocityC2SPacket(
    val velocity: Vec3d,
) : FabricPacket {
    companion object {
        val id = id("update_velocity_c2s")
        val pType = PacketType.create(id) {
            UpdateVelocityC2SPacket(Vec3d(it.readDouble(), it.readDouble(), it.readDouble()))
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                player.velocity = packet.velocity
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeDouble(velocity.x)
        buf.writeDouble(velocity.y)
        buf.writeDouble(velocity.z)
    }

    override fun getType(): PacketType<*> = pType
}