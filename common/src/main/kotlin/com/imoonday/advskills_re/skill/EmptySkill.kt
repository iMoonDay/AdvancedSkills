package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.text.*

class EmptySkill : Skill(
    id = id("empty"),
    name = translateSkill("empty", "name"),
    description = Text.empty(),
    rarity = SkillRarity.USELESS,
    invalid = true
) {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.fail(Text.empty())

    override fun getItemTooltips(displayName: Boolean, displayId: Boolean): List<Text> = emptyList()
}