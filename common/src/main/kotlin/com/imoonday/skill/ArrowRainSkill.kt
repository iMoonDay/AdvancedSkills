package com.imoonday.skill

import com.imoonday.entity.*
import com.imoonday.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*

class ArrowRainSkill : Skill(
    id = "arrow_rain",
    types = listOf(SkillType.ATTACK),
    cooldown = 15,
    rarity = Rarity.RARE,
    sound = SoundEvents::ENTITY_ARROW_SHOOT
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val raycast = user.raycast(256.0, 0f, true)
        val center = if (raycast.type == HitResult.Type.MISS) user.pos else raycast.pos
        val random = user.random
        for (i in 0 until 5) {
            val amount = random.nextInt(51) + 50
            val range = amount * 0.25
            for (j in 0 until amount) {
                user.world.spawnEntity(
                    UngroundedArrowEntity(
                        user.world,
                        center.x + random.nextDouble() * range - range / 2,
                        center.y + 20 + i * 10 * random.nextDouble(),
                        center.z + random.nextDouble() * range - range / 2,
                        user
                    ).apply { pitch = -90f })
            }
        }
        return UseResult.success()
    }
}