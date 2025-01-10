package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class TimeRewindSkill : LongPressSkill(
    Settings(
        id = "time_rewind",
        types = listOf(SkillType.RESTORATION, SkillType.MOVEMENT),
        cooldown = 60,
        rarity = SkillRarity.MYTHIC
    )
), UsingRenderTrigger, DeathTrigger {

    init {
        settings
            .addParameter(PARAM_REWIND_SOUND, DEFAULT_REWIND_SOUND)
            .addParameter(PARAM_HEAL_SOUND, DEFAULT_HEAL_SOUND)
            .addParameter(
                name = PARAM_REWIND_DURATION,
                baseValue = DEFAULT_REWIND_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing {
            NbtUtils.writeEntityPositionToTag(player.pos, it)
            it.putString(NBT_DIMENSION, player.world.registryKey.value.toString())
            it.putFloat(NBT_HEALTH_RATIO, player.health / player.maxHealth)
        }
        return getChargingResult()
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        val data = player.getActiveData()
        NbtUtils.readEntityPositionFromTag(data)?.let {
            data.getString(NBT_DIMENSION).toIdentifier()?.let { id ->
                player.server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id))?.let { world ->
                    player.playSoundFromParam(PARAM_REWIND_SOUND, DEFAULT_REWIND_SOUND)
                    player.teleport(world, it.x, it.y, it.z, emptySet(), player.yaw, player.pitch)
                    player.playSoundFromParam(PARAM_REWIND_SOUND, DEFAULT_REWIND_SOUND)
                }
            }
        }
        if (data.contains(NBT_HEALTH_RATIO)) {
            player.health = data.getFloat(NBT_HEALTH_RATIO) * player.maxHealth
            player.playSoundFromParam(PARAM_HEAL_SOUND, DEFAULT_HEAL_SOUND.get())
        }
        player.fallDistance = 0f
        player.stopUsing()
        return UseResult.success()
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_REWIND_DURATION, player, DEFAULT_REWIND_DURATION, 0)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean =
        if (player.isUsing()) {
            onRelease(player, player.getUsedTime())
            false
        } else true

    companion object {

        // Default Values
        private const val DEFAULT_REWIND_DURATION = 5 * 20
        private val DEFAULT_REWIND_SOUND = SoundEvents.ENTITY_FOX_TELEPORT
        private val DEFAULT_HEAL_SOUND = ModSounds.HEAL

        // Parameter Names
        private const val PARAM_REWIND_SOUND = "rewind_sound"  // 时间回溯音效
        private const val PARAM_HEAL_SOUND = "heal_sound"  // 治疗音效
        private const val PARAM_REWIND_DURATION = "rewind_duration"  // 回溯持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间

        // NBT Keys
        private const val NBT_DIMENSION = "Dimension"  // 维度
        private const val NBT_HEALTH_RATIO = "HealthRatio"  // 生命值比例
    }
}