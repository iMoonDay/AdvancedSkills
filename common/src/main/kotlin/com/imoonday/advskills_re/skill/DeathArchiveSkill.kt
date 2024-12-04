package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import com.imoonday.advskills_re.util.UseResult.Companion.consume
import com.imoonday.advskills_re.util.UseResult.Companion.fail
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*

class DeathArchiveSkill : Skill(
    id = "death_archive",
    types = listOf(SkillType.DEFENSE, SkillType.RESTORATION),
    cooldown = 300,
    rarity = Rarity.UNIQUE
), UsingProgressTrigger, DeathTrigger, DamageTrigger, TickTrigger, UnequipTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isUsing() && user.isInInvulnerableState()) {
            return fail(failedMessage())
        }
        val active = user.toggleUsing(this, NbtCompound().apply { NbtUtils.writeEntityGlobalPosToTag(user, this) })
        return consume(translateActive(this, active))
    }

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean =
        if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && player.isUsing()) {
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
                        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT)
                        player.teleport(world, pos.x, pos.y, pos.z, emptySet(), player.yaw, player.pitch)
                        while (!world.isSpaceEmpty(player) && player.y < world.topY) {
                            player.teleport(player.x, player.y + 1.0, player.z)
                        }
                        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT)
                    }
                }
                player.getActiveData().putBoolean("invulnerable", true)
                player.resetUsedTime(this)
            }
            false
        } else true

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing() && player.isInInvulnerableState() && usedTime >= 5 * 20) {
            player.stopAndCooldown()
        }
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?
    ): Boolean = player.isUsing() && player.isInInvulnerableState()

    private fun PlayerEntity.isInInvulnerableState(): Boolean = getActiveData().getBoolean("invulnerable")

    override fun getProgress(player: PlayerEntity): Double = if (player.isUsing()) {
        if (player.isInInvulnerableState()) 1 - player.getUsedTime() / (5 * 20.0)
        else player.health.toDouble() / player.maxHealth
    } else 0.0

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        if (player.isUsing() && player.isInInvulnerableState()) {
            player.startCooling()
        }
    }
}