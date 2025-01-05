package com.imoonday.advskills_re.item

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.item.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*

class SkillFruitItem(val rarity: SkillRarity, settings: Settings) : Item(settings) {

    constructor(rarity: SkillRarity) : this(
        rarity,
        Settings().food(FoodComponent.Builder().alwaysEdible().build())
    )

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        if (user is ServerPlayerEntity) {
            var result = user.learnRandomly { it.rarity.level <= rarity.level }
            if (!result) {
                result = user.enhanceRandomly { skill, _ -> skill.rarity.level <= rarity.level }
            }

            if (!result) {
                user.sendMessage(translate("learnSkill.noLearnableSkills"), true)
            }
        }
        if (user !is PlayerEntity || !user.abilities.creativeMode) {
            stack.decrement(1)
        }
        return stack
    }

    override fun getName(): Text = rarity.format(nameText)

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