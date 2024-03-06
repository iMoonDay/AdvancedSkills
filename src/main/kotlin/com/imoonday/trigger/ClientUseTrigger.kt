package com.imoonday.trigger

import net.minecraft.client.MinecraftClient

interface ClientUseTrigger {

    fun onUse(client: MinecraftClient)

    fun onStop(client: MinecraftClient)
}