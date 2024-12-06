package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*

class ThunderFurySkill : Skill(
    id = "thunder_fury",
    types = listOf(SkillType.ATTACK),
    cooldown = 15,
    rarity = SkillRarity.EPIC,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val result = user.raycastBlock(512.0)
        if (result.type != HitResult.Type.BLOCK) return UseResult.fail(failedMessage())

        EntityType.LIGHTNING_BOLT.create(user.world)?.let {
            user.world.spawnEntity(it.apply {
                refreshPositionAfterTeleport(result.pos)
            })
        }
        return UseResult.success()
    }
}