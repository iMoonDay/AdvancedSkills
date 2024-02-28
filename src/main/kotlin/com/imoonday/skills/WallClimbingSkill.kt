package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.toggleUsingSkill
import com.imoonday.triggers.ClimbingTrigger
import com.imoonday.triggers.PersistentTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.translateSkill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class WallClimbingSkill : Skill(
    id = "wall_climbing",
    types = arrayOf(SkillType.PASSIVE, SkillType.MOVEMENT),
    rarity = Rarity.RARE,
), ClimbingTrigger, PersistentTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        val active = user.toggleUsingSkill(this)
        return UseResult.consume(
            translateSkill(
                id.path, if (active) "active" else "inactive",
                name.string
            )
        )
    }

    override fun isClimbing(player: PlayerEntity): Boolean =
        if (!player.isUsingSkill(this)) false else player.horizontalCollision
}