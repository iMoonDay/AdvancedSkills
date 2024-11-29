package com.imoonday.advskills_re.item

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.item.*

import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*

class SkillFruitItem(val rarity: Skill.Rarity, settings: Settings) : Item(settings) {

    constructor(rarity: Skill.Rarity) : this(
        rarity,
        Settings().food(FoodComponent.Builder().alwaysEdible().build())
    )

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        (user as? ServerPlayerEntity)?.learnRandomly { it.getRarity(world).level <= rarity.level }
        stack.decrement(1)
        return stack
    }

    override fun getName(): Text = nameText.formatted(rarity.formatting)

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

    companion object {

        private val nameText = Text.translatable("item.${MOD_ID}.skill_fruit")
    }
}