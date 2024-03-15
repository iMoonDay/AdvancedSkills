package com.imoonday.custom

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import net.minecraft.entity.player.PlayerEntity

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("value")
@Serializable
sealed interface Task {

    val type: String
    val data: Map<String, String>

    fun execute(player: PlayerEntity): Any
}