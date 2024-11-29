package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import net.minecraft.world.*

class EmptySkill : Skill(
    id = id("empty"),
    name = translateSkill("empty", "name"),
    description = Text.empty(),
    rarity = Rarity.USELESS,
    invalid = true
) {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.fail(Text.empty())

    override fun getItemTooltips(world: World?, displayName: Boolean): List<Text> = emptyList()
}