package com.imoonday.item

import com.imoonday.skill.Skill
import com.imoonday.util.client
import com.imoonday.util.learn
import com.imoonday.util.translate
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