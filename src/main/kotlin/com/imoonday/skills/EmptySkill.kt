package com.imoonday.skills

import com.imoonday.utils.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class EmptySkill : Skill(
    id = id("empty"),
    name = translateSkill("empty", "name"),
    description = Text.empty(),
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.USELESS,
    isEmpty = true
) {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.fail(Text.empty())

    override val tooltips: List<Text>
        get() = listOf(name)
}