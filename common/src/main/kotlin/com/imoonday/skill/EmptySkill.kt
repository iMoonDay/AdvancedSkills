package com.imoonday.skill

import com.imoonday.util.UseResult
import com.imoonday.util.id
import com.imoonday.util.translateSkill
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

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