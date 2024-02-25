package com.imoonday.skills

import com.imoonday.AdvancedSkills
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.TranslationUtil
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class EmptySkill : Skill(
    id = AdvancedSkills.id("empty"),
    name = TranslationUtil.skillName("empty"),
    description = Text.empty(),
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.USELESS,
    isEmpty = true
) {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.fail(Text.empty())

    override val tooltips: List<Text>
        get() = listOf(name)
}