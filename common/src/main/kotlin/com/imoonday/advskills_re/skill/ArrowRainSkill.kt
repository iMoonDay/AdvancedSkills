package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*

class ArrowRainSkill : Skill(
    Settings.loadOrCreate {
        Settings(
            id = "arrow_rain",
            types = listOf(SkillType.ATTACK, SkillType.SUMMON),
            cooldown = 15,
            rarity = SkillRarity.RARE
        ).addParameter("max_distance", 256.0)
            .addParameter("min_summon_amount", 50)
            .addParameter("max_summon_amount", 100)
            .addParameter("summon_interval", 2)
            .addParameter("launch_sound", SoundEvents.ENTITY_ARROW_SHOOT.id.toString())
            .addEnhanceableParameter(
                "arrow_damage",
                2.0,
                "damage",
                0.2f,
                Enhancement.Type.MULTIPLY,
                5
            ).addEnhanceableParameter("launch_count", 5, "count", 1f, Enhancement.Type.ADDITION, 5)
            .addEnhanceableParameter("summon_range", 20.0, "range", 2f, Enhancement.Type.ADDITION, 5)
            .addEnhancement("summon_amount", 10f, Enhancement.Type.ADDITION, 5)
    }
) {

    init {
        addEnhancementDescArg("damage") { (it * 100).toInt() }
        addEnhancementDescArg("count") { it.toInt() }
        addEnhancementDescArg("range") { it.toInt() }
        addEnhancementDescArg("summon_amount") { it.toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val damage = user.getDoubleParameter("arrow_damage")
        val maxDistance = user.getDoubleParameter("max_distance")
        val raycast = user.raycast(maxDistance, 0f, true)
        val center = if (raycast.type == HitResult.Type.MISS) user.pos else raycast.pos
        val remainingTimes = user.getIntParameter("launch_count") - 1
        val range = user.getDoubleParameter("summon_range")
        val amount = user.getEnhancementValue("summon_amount").toInt()
        val (min, max) = user.getIntParameter("min_summon_amount") to user.getIntParameter("max_summon_amount")
        val interval = user.getIntParameter("summon_interval")
        val sound = Registries.SOUND_EVENT.get(user.getStringParameter("launch_sound").toIdentifier())
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