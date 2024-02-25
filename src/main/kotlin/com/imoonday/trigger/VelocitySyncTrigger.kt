package com.imoonday.trigger

import com.imoonday.network.UpdateVelocityC2SPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.network.ClientPlayerEntity

interface VelocitySyncTrigger {

    fun syncVelocity(player: ClientPlayerEntity) {
        ClientPlayNetworking.send(UpdateVelocityC2SPacket(player.velocity))
    }
}