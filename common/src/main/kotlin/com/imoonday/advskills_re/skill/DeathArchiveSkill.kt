package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import com.imoonday.advskills_re.util.UseResult.Companion.consume
import com.imoonday.advskills_re.util.UseResult.Companion.fail
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*

class DeathArchiveSkill : Skill(
    Settings(
        id = "death_archive",
        types = listOf(SkillType.DEFENSE, SkillType.RESTORATION),
        cooldown = 300,
        rarity = SkillRarity.UNIQUE
    )
), UsingProgressTrigger, DeathTrigger, DamageTrigger, TickTrigger, UnequipTrigger, UsingRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_TELEPORT_SOUND, DEFAULT_TELEPORT_SOUND)
            .addParameter(
                name = PARAM_INVULNERABLE_DURATION,
                baseValue = DEFAULT_INVULNERABLE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isUsing() && user.isInInvulnerableState()) {
            return fail(failedMessage())
        }
        val active = user.toggleUsing(this, NbtCompound().apply { NbtUtils.writeEntityGlobalPosToTag(user, this) })
        return consume(translateActive(this, active))
    }

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean =
        if (player.isUsing()) {
            if (!player.isInInvulnerableState()) {
                player.health = player.maxHealth
                player.fallDistance = 0f
                player.clearStatusEffects()
                player.isOnFire = false
                player.fireTicks = 0
                player.world.sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING)
                NbtUtils.readGlobalPosFromTag(player.getActiveData()).ifPresent { globalPos ->
                    player.server.getWorld(globalPos.dimension)?.let { world ->
                        val pos = Vec3d.ofBottomCenter(globalPos.pos)
                        player.playSoundFromParam(PARAM_TELEPORT_SOUND, DEFAULT_TELEPORT_SOUND)
                        player.teleport(world, pos.x, pos.y, pos.z, emptySet(), player.yaw, player.pitch)
                        while (!world.isSpaceEmpty(player) && player.y < world.topY) {
                            player.teleport(player.x, player.y + 1.0, player.z)
                        }
                        player.playSoundFromParam(PARAM_TELEPORT_SOUND, DEFAULT_TELEPORT_SOUND)
                    }
                }
                player.getActiveData().putBoolean(NBT_INVULNERABLE_STATE, true)
                player.resetUsedTime(this)
            }
            false
        } else {
            true
        }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        val maxTime = getMaxInvulnerableTime(player)
        if (player.isUsing() && player.isInInvulnerableState() && usedTime >= maxTime) {
            player.stopAndCooldown()
        }
    }

    private fun getMaxInvulnerableTime(player: PlayerEntity) =
        getIntParam(PARAM_INVULNERABLE_DURATION, player, DEFAULT_INVULNERABLE_DURATION)

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?
    ): Boolean = player.isUsing() && player.isInInvulnerableState()

    private fun PlayerEntity.isInInvulnerableState(): Boolean =
        getActiveData().getBoolean(NBT_INVULNERABLE_STATE)

    override fun getProgress(player: PlayerEntity): Double =
        if (player.isUsing()) {
            if (player.isInInvulnerableState()) {
                1.0 - player.getUsedTime() / getMaxInvulnerableTime(player).toDouble()
            } else {
                player.health.toDouble() / player.maxHealth
            }
        } else {
            0.0
        }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        if (player.isUsing() && player.isInInvulnerableState()) {
            player.startCooling()
        }
    }

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean =
        super.shouldRenderFeature(target, clientPlayer) && target.isInInvulnerableState()

    companion object {

        // NBT Keys
        private const val NBT_INVULNERABLE_STATE = "Invulnerable"

        // Default Values
        private const val DEFAULT_INVULNERABLE_DURATION = 5 * 20
        private val DEFAULT_TELEPORT_SOUND = SoundEvents.ENTITY_ENDERMAN_TELEPORT

        // Parameter Names
        private const val PARAM_TELEPORT_SOUND = "teleport_sound"  // 传送音效
        private const val PARAM_INVULNERABLE_DURATION = "invulnerable_duration"  // 无敌时长

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应无敌时长
    }
}