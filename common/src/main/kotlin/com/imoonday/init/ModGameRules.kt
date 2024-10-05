package com.imoonday.init

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule
import net.minecraft.world.GameRules

object ModGameRules {

    @JvmField
    val COOLDOWN_MULTIPLIER: GameRules.Key<DoubleRule> =
        GameRuleRegistry.register(
            "skillCooldownMultiplier",
            GameRules.Category.PLAYER,
            GameRuleFactory.createDoubleRule(1.0, 0.0)
        )

    fun init() = Unit
}