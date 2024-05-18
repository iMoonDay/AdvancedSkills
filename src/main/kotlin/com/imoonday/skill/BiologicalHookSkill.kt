package com.imoonday.skill

import com.imoonday.entity.HookEntity
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.plus
import com.imoonday.util.times
import net.minecraft.server.network.ServerPlayerEntity

class BiologicalHookSkill : Skill(
    id = "biological_hook",
    types = listOf(SkillType.CONTROL),
    cooldown = 15,
    rarity = Rarity.SUPERB,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(HookEntity(user.world, user).apply {
            velocity = user.rotationVector
            setPosition(user.eyePos + user.rotationVector * 0.5)
        })
        return UseResult.success()
    }
}