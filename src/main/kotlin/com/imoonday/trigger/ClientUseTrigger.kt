package com.imoonday.trigger

import net.minecraft.client.MinecraftClient

interface ClientUseTrigger : SkillTrigger {

    fun onUse(client: MinecraftClient) = Unit

    fun onStop(client: MinecraftClient) = Unit
}