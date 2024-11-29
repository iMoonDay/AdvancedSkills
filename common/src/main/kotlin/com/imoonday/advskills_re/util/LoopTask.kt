package com.imoonday.advskills_re.util

import net.minecraft.server.network.*

class LoopTask(
    val task: (ServerPlayerEntity) -> Boolean,
    var duration: Int = Int.MAX_VALUE,
    var delay: Int = 0,
    var maxExecutions: Int = Int.MAX_VALUE,
    var priority: Int = 0,
    var onComplete: ((ServerPlayerEntity) -> Unit)? = null
) {

    var isPaused = false
        private set
    var executions = 0
        private set
    var startCondition: ((ServerPlayerEntity) -> Boolean)? = null
    var stopCondition: ((ServerPlayerEntity) -> Boolean)? = null

    fun tick(player: ServerPlayerEntity): Boolean {
        if (isPaused) {
            return true
        }

        startCondition?.let {
            if (!it(player)) {
                return true
            }
            startCondition = null
        }

        stopCondition?.let {
            if (it(player)) {
                return false
            }
        }

        if (delay > 0) {
            delay--
            return true
        }

        if (!task(player)) return false

        if (++executions >= maxExecutions) {
            return false
        }

        if (--duration <= 0) {
            onComplete?.invoke(player)
            return false
        }

        return true
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }
}