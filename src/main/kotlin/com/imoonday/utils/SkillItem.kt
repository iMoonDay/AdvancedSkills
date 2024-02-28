package com.imoonday.utils

import com.imoonday.components.learnSkill
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class SkillItem(val skill: Skill, settings: Settings) : Item(settings) {
    constructor(skill: Skill) : this(skill, FabricItemSettings().maxCount(1))

    override fun getName(): Text {
        return skill.formattedName
    }

    override fun getName(stack: ItemStack): Text {
        return skill.formattedName
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext,
    ) {
        tooltip.add(skill.description.copy().formatted(Formatting.GRAY))
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (world.isClient) return TypedActionResult.success(stack)
        if (user.learnSkill(skill)) {
            stack.decrement(1)
            return TypedActionResult.success(stack)
        }
        user.sendMessage(translate("learnSkill", "failedSelf", skill.name.string))
        return TypedActionResult.fail(stack)
    }
}