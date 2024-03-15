package com.imoonday.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

@Serializable
@SerialName("message")
class MessageAction(
    override val data: Map<String, String> = mutableMapOf(),
) : Action {

    override fun execute(player: PlayerEntity) {
        data["value"]?.let {
            player.sendMessage(Text.literal(it), data["overlay"] == "true")
        }
    }
}