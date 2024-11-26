package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.raycastVisualBlock
import net.minecraft.entity.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*

class ThunderFurySkill : Skill(
    id = "thunder_fury",
    types = listOf(SkillType.ATTACK),
    cooldown = 15,
    rarity = Rarity.EPIC,
) {

    override fun use(user: ServerPlayerEntity): UseResult =
        if (user.raycastVisualBlock(512.0).type == HitResult.Type.BLOCK) {
            EntityType.LIGHTNING_BOLT.create(user.world)?.let {
                user.world.spawnEntity(it.apply {
                    refreshPositionAfterTeleport(user.raycastVisualBlock(512.0).pos)
                })
            }
            UseResult.success()
        } else UseResult.fail(failedMessage())
}