package com.imoonday.advskills_re.item

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.item.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*

class SkillItem(val skill: Skill, settings: Settings) : Item(settings) {
    constructor(skill: Skill) : this(skill, Settings().maxCount(1))

    override fun getName(): Text {
        val name = skill.formattedName
        return if (skill.invalid) name.formatted(Formatting.STRIKETHROUGH) else name
    }

    override fun getName(stack: ItemStack): Text = name

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext,
    ) {
        tooltip.addAll(skill.getItemTooltips())
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (world.isClient) return TypedActionResult.success(stack)
        if (skill.invalid) {
            user.sendMessage(translate("learnSkill.invalid", skill.name))
            return TypedActionResult.fail(stack)
        }
        if (user.learn(skill)) {
            if (!user.abilities.creativeMode) {
                stack.decrement(1)
            }
            return TypedActionResult.success(stack)
        }
        user.sendMessage(translate("learnSkill.failedSelf", skill.name))
        return TypedActionResult.fail(stack)
    }
}