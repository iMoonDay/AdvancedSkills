package com.imoonday.advskills_re.util

/**
 * Represents a loop task that repeats based on the given interval and repeat count.
 */
class LoopTask(
    private val interval: Int,
    private var repeat: Int,
    private val task: () -> Boolean
) {

    private var remaining: Int = interval

    @Volatile
    var finished: Boolean = false
        private set

    fun execute() {
        if (finished) return

        if (remaining > 0) {
            remaining--
            return
        }

        val result = task()
        if (!result || --repeat <= 0) {
            finished = true
            return
        }

        remaining = interval
    }

    fun cancel() {
        finished = true
    }
}