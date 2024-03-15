package com.imoonday.custom

import com.imoonday.util.UseResult
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity

@Serializable
data class Event(
    val tasks: List<Task>,
    val triggers: List<Trigger>,
) {

    fun run(player: PlayerEntity): UseResult {
        tasks.forEach {
            when (it) {
                is Action -> it.execute(player)
                is Condition -> if (it.execute(player)) it.actions.run(player) else it.failedActions?.run(player)
                is Return -> return it.execute(player)
                is Trigger -> {}
            }
        }
        return UseResult.fail()
    }
}