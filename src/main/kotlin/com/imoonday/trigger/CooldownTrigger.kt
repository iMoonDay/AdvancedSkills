package com.imoonday.trigger

interface CooldownTrigger {

    fun getCooldown(original: Int): Int
}