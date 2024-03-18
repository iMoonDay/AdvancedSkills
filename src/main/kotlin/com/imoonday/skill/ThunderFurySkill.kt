package com.imoonday.skill

import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.raycastVisualBlock
import net.minecraft.entity.EntityType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult

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