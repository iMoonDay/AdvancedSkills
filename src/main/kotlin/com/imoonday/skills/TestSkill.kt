package com.imoonday.skills

import com.imoonday.utils.id
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class TestSkill(
    index: Int,
) : Skill(
    id = id("test_$index"),
    name = Text.literal("Test $index"),
    description = Text.literal(generateRandomString((1..100).random())),
    cooldown = (0..20 * 10).random(),
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.entries.random()
) {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.success()
}

fun generateRandomString(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { charPool.random() }
        .joinToString("")
}