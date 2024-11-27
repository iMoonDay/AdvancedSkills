package com.imoonday.advskills_re.network.s2c

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.util.*
import dev.architectury.networking.*
import dev.architectury.utils.*
import net.minecraft.network.*
import net.minecraft.network.packet.s2c.play.*

class SpawnParticlesS2CPacket(
    private val particles: List<ParticleS2CPacket>
) : NetworkPacket {

    constructor(buf: PacketByteBuf) : this(buf.readList(::ParticleS2CPacket))

    override fun encode(buf: PacketByteBuf) = buf.writeCollection(particles) { buf1, packet -> packet.write(buf1) }

    override fun apply(context: NetworkManager.PacketContext) {
        if (context.environment == Env.SERVER) return
        context.queue {
            client?.networkHandler?.run {
                for (packet in particles) {
                    packet.apply(this)
                }
            }
        }
    }
}