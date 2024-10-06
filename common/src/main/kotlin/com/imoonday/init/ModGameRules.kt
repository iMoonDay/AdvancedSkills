package com.imoonday.init

import net.minecraft.world.*

object ModGameRules {

    @JvmField
    val COOLDOWN_MULTIPLIER = register(
        "skillCooldownMultiplier",
        GameRules.Category.PLAYER,
        GameRules.IntRule.create(100)
    )

    fun init() = Unit

    private fun <T : GameRules.Rule<T>> register(
        id: String,
        type: GameRules.Category,
        factory: GameRules.Type<T>
    ): GameRules.Key<T> = GameRules.register(id, type, factory)
}