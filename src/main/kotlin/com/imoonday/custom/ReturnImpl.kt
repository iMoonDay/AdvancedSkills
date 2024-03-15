package com.imoonday.custom

import com.imoonday.util.UseResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
@SerialName("return")
class ReturnImpl(
    private val result: UseResult,
) : Return {

    override val data: Map<String, String> = emptyMap()
    override fun execute(player: PlayerEntity): UseResult = result
}