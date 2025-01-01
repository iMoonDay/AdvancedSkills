package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.text.*

class EmptySkill : Skill(
    Settings(
        id = id("empty"),
        name = translateSkill("empty", "name"),
        description = Text.empty(),
        rarity = SkillRarity.UNKNOWN,
        invalid = true,
    )
) {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.fail(Text.empty())

    override fun getItemTooltips(displayName: Boolean, displayId: Boolean): MutableList<Text> = mutableListOf()

    override fun updateSettings(settings: Settings) = Unit

    override fun resetSettings() = Unit
}