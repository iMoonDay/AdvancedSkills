package com.imoonday.triggers

interface CooldownTrigger {

    fun getCooldown(original: Int): Int
}