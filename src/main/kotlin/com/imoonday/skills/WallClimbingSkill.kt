package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.toggleUsingSkill
import com.imoonday.trigger.ClimbingTrigger
import com.imoonday.trigger.PersistentTrigger
import com.imoonday.utils.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class WallClimbingSkill : Skill(
    id = "wall_climbing",
    types = arrayOf(SkillType.PASSIVE, SkillType.MOVEMENT),
    rarity = Rarity.RARE,
), ClimbingTrigger, PersistentTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        val active = user.toggleUsingSkill(this)
        return UseResult.consume(
            Text.translatable(
                "advancedSkills.skill.wall_climbing.${if (active) "active" else "inactive"}",
                name.string
            )
        )
    }

    override fun isClimbing(player: PlayerEntity): Boolean =
        if (!player.isUsingSkill(this)) false else player.horizontalCollision
}