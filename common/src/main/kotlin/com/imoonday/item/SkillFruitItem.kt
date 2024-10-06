package com.imoonday.item

import com.imoonday.skill.*
import com.imoonday.util.*
import net.minecraft.client.item.*
import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*

class SkillFruitItem(val rarity: Skill.Rarity, settings: Settings) : Item(settings) {

    private val translationKey = "item.advanced_skills.skill_fruit"

    constructor(rarity: Skill.Rarity) : this(
        rarity,
        Settings().food(FoodComponent.Builder().alwaysEdible().build())
    )

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        (user as? ServerPlayerEntity)?.learnRandomly { it.rarity.level <= rarity.level }
        stack.decrement(1)
        return stack
    }

    override fun getName(): Text = Text.translatable(translationKey).formatted(rarity.formatting)

    override fun getName(stack: ItemStack): Text = name

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext,
    ) {
        if (rarity.level > 0) tooltip.add(rarity.displayName.copy().formatted(Formatting.GRAY))
        super.appendTooltip(stack, world, tooltip, context)
    }
}