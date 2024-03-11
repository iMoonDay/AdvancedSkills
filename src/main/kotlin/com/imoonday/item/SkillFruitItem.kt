package com.imoonday.item

import com.imoonday.component.learnRandomSkill
import com.imoonday.skill.Skill
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World

class SkillFruitItem(val rarity: Skill.Rarity, settings: Settings) : Item(settings) {

    private val translationKey = "item.advanced_skills.skill_fruit"

    constructor(rarity: Skill.Rarity) : this(
        rarity,
        FabricItemSettings().food(FoodComponent.Builder().alwaysEdible().build())
    )

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        (user as? ServerPlayerEntity)?.learnRandomSkill { it.rarity.level <= rarity.level }
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