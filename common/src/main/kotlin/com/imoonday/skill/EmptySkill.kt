package com.imoonday.skill

import com.imoonday.util.*
import net.minecraft.client.*
import net.minecraft.server.network.*
import net.minecraft.text.*

class EmptySkill : Skill(
    id = id("empty"),
    name = translateSkill("empty", "name"),
    description = Text.empty(),
    rarity = Rarity.USELESS,
    invalid = true
) {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.fail(Text.empty())

    override fun getItemTooltips(client: MinecraftClient, displayName: Boolean): List<Text> = emptyList()
}