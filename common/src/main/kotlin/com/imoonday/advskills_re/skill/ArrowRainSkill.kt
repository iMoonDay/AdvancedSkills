package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*

class ArrowRainSkill : Skill(
    Settings(
        id = "arrow_rain",
        types = listOf(SkillType.ATTACK, SkillType.SUMMON),
        cooldown = 15,
        rarity = SkillRarity.RARE
    )
) {

    init {
        this.settings
            .addParameter("max_distance", 256.0)
            .addParameter("min_summon_amount", 50)
            .addParameter("max_summon_amount", 100)
            .addParameter("summon_interval", 2)
            .addParameter("launch_sound", SoundEvents.ENTITY_ARROW_SHOOT)

        addEnhanceableParameter(
            name = "arrow_damage",
            baseValue = 2.0,
            enhancementId = "damage",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addEnhanceableParameter(
            name = "launch_count",
            baseValue = 5,
            enhancementId = "count",
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT
        )

        addEnhanceableParameter(
            name = "summon_range",
            baseValue = 20.0,
            enhancementId = "range",
            value = 2.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5
        )

        addEnhancement(
            id = "summon_amount",
            value = 10.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val damage = getDoubleParam("arrow_damage", user, 2.0)
        val maxDistance = getDoubleParam("max_distance", user, 256.0)
        val raycast = user.raycast(maxDistance, 0f, true)
        val center = if (raycast.type == HitResult.Type.MISS) user.pos else raycast.pos
        val remainingTimes = getIntParam("launch_count", user, 5)
        val range = getDoubleParam("summon_range", user, 20.0)
        val amount = user.getEnhancementValue("summon_amount").toInt()
        val (min, max) = getIntParam("min_summon_amount", user, 50) to getIntParam("max_summon_amount", user, 100)
        val interval = getIntParam("summon_interval", user, 2)
        val sound = getSoundEventParam("launch_sound", SoundEvents.ENTITY_ARROW_SHOOT)
        user.executeAndAddTask(interval, remainingTimes) {
            spawnArrows(user, center, damage, range, min, max, amount, sound)
        }
        return UseResult.success()
    }

    private fun spawnArrows(
        user: ServerPlayerEntity,
        center: Vec3d,
        damage: Double,
        range: Double,
        min: Int,
        max: Int,
        extraAmount: Int,
        sound: SoundEvent?
    ): Boolean {
        val random = user.random
        val amount = random.nextBetween(min, max) + extraAmount

        sound?.let { user.serverWorld.playSound(null, center.x, center.y, center.z, it, SoundCategory.VOICE, 1f, 1f) }
        var result = false
        val particles: MutableList<ParticleS2CPacket> = mutableListOf()
        repeat(amount) {
            user.world.spawnEntity(
                UngroundedArrowEntity(
                    user.world,
                    center.x + random.nextDouble() * range - range / 2,
                    center.y + 20,
                    center.z + random.nextDouble() * range - range / 2,
                    user
                ).apply {
                    pitch = -90f
                    if (this.damage != damage) {
                        this.damage = damage
                    }
                }.also {
                    particles.add(
                        ParticleS2CPacket(
                            ParticleTypes.CLOUD,
                            false,
                            it.x, it.y, it.z,
                            1f, 0f, 1f,
                            0f, 5
                        )
                    )
                }).also {
                if (it && !result) {
                    result = true
                }
            }
        }
        if (particles.isNotEmpty()) {
            user.sendPacket(BundleS2CPacket(particles))
        }
        return result
    }
}