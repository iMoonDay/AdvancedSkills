package com.imoonday.item

import com.imoonday.skill.*
import com.imoonday.util.*
import net.minecraft.client.item.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*

class SkillItem(val skill: Skill, settings: Settings) : Item(settings) {
    constructor(skill: Skill) : this(skill, Settings().maxCount(1))

    override fun getName(): Text =
        if (skill.invalid) skill.formattedName.copy().formatted(Formatting.STRIKETHROUGH) else skill.formattedName

    override fun getName(stack: ItemStack): Text =
        if (skill.invalid) skill.formattedName.copy().formatted(Formatting.STRIKETHROUGH) else skill.formattedName

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext,
    ) {
        if (world?.isClient == true) tooltip.addAll(skill.getItemTooltips(client!!))
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (world.isClient) return TypedActionResult.success(stack)
        if (skill.invalid) {
            user.sendMessage(translate("learnSkill", "invalid", skill.name.string))
            return TypedActionResult.fail(stack)
        }
        if (user.learn(skill)) {
            stack.decrement(1)
            return TypedActionResult.success(stack)
        }
        user.sendMessage(translate("learnSkill", "failedSelf", skill.name.string))
        return TypedActionResult.fail(stack)
    }
}