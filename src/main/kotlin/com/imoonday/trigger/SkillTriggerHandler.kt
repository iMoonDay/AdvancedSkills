package com.imoonday.trigger

import com.imoonday.component.*
import com.imoonday.network.SendPlayerDataC2SPacket
import com.imoonday.skill.Skill
import com.imoonday.trigger.SendPlayerDataTrigger.SendTime.*
import com.imoonday.util.SkillSlot
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object SkillTriggerHandler {

    fun onLanding(player: ServerPlayerEntity, height: Float) =
        player.equippedSkills
            .filterIsInstance<LandingTrigger>()
            .forEach { it.onLanding(player, height) }

    fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        var newAmount = amount
        player.equippedSkills
            .filterIsInstance<DamageTrigger>()
            .forEach { newAmount = it.onDamaged(newAmount, source, player, attacker) }
        return newAmount
    }

    fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.equippedSkills
        .filterIsInstance<DamageTrigger>()
        .map { it.ignoreDamage(amount, source, player, attacker) }
        .any { it }

    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float {
        var newAmount = amount
        player.equippedSkills
            .filterIsInstance<AttackTrigger>()
            .forEach { newAmount = it.onAttack(newAmount, source, player, target) }
        return newAmount
    }

    fun tick(player: ServerPlayerEntity) = player.run {
        equippedSkills.filter { it is AutoStartTrigger }
            .filterNot { it in usingSkills }
            .forEach { onStart(player, it) }
        equippedSkills
            .filterIsInstance<AutoTrigger>()
            .forEach { it.tick(this) }
        equippedSkills.filter { it is TickTrigger }
            .forEach { (it as TickTrigger).tick(this, getSkillUsedTime(it)) }
    }

    fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        var newAmount = amount
        player.equippedSkills
            .filterIsInstance<FallTrigger>()
            .forEach { newAmount = it.onFall(newAmount, player, fallDistance, damageMultiplier) }
        return newAmount
    }

    fun onStart(player: ServerPlayerEntity, skill: Skill) {
        if (skill is AutoStartTrigger) {
            skill.onStart(player)
            player.startUsingSkill(skill)
        }
    }

    fun postMine(world: World, block: BlockState, pos: BlockPos, miner: PlayerEntity, item: ItemStack) {
        miner.equippedSkills
            .filterIsInstance<MiningTrigger>()
            .forEach { it.postMine(world, block, pos, miner, item) }
    }

    fun postHit(target: LivingEntity, attacker: PlayerEntity, item: ItemStack) {
        attacker.equippedSkills
            .filterIsInstance<HitTrigger>()
            .forEach { it.postHit(target, attacker, item) }
    }

    fun allowClimbing(player: PlayerEntity): Boolean =
        player.equippedSkills
            .filterIsInstance<ClimbingTrigger>()
            .map { it.isClimbing(player) }
            .any { it }

    fun onEquipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? EquipTrigger)?.onEquipped(player, slot) ?: true

    fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? EquipTrigger)?.postEquipped(player, slot)

    fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? UnequipTrigger)?.onUnequipped(player, slot) ?: true

    fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? UnequipTrigger)?.postUnequipped(player, slot)

    fun allowWalkOnFluid(player: PlayerEntity, fluidState: FluidState) =
        player.equippedSkills
            .filterIsInstance<WalkOnFluidTrigger>()
            .map { it.canWalkOnFluid(player, fluidState) }
            .any { it }

    fun getHeartType(player: PlayerEntity): HeartType? =
        player.equippedSkills
            .filterIsInstance<HeartTypeTrigger>()
            .mapNotNull { it.getHeartType(player) }
            .maxByOrNull { it.second }?.first

    fun getStepHeight(player: PlayerEntity): Float? {
        return player.equippedSkills
            .filterIsInstance<StepHeightTrigger>()
            .mapNotNull { it.getStepHeight(player) }
            .maxOrNull()
    }

    fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double {
        var newSpeed = speed
        player.equippedSkills
            .filterIsInstance<FluidMovementTrigger>()
            .forEach { newSpeed = it.getMovementInFluid(player, tag, newSpeed) }
        return newSpeed
    }

    fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean {
        return player.equippedSkills
            .filterIsInstance<FluidMovementTrigger>()
            .map { it.ignoreFluid(player, tag) }
            .any { it }
    }

    fun canBreatheInWater(player: PlayerEntity): Boolean {
        return player.equippedSkills
            .filterIsInstance<BreatheInWaterTrigger>()
            .map { it.canBreatheInWater(player) }
            .any { it }
    }

    fun isInvisible(player: PlayerEntity): Boolean {
        return player.equippedSkills
            .filterIsInstance<InvisibilityTrigger>()
            .map { it.isInvisible(player) }
            .any { it }
    }

    fun isInvisibleTo(player: PlayerEntity, otherPlayer: PlayerEntity): Boolean {
        return player.equippedSkills
            .filterIsInstance<InvisibilityTrigger>()
            .map { it.isInvisibleTo(player, otherPlayer) }
            .any { it }
    }

    fun sendPlayerData(player: ClientPlayerEntity) {
        player.learnedSkills
            .filter { it is SendPlayerDataTrigger && it.getSendTime() != USE }
            .forEach {
                it as SendPlayerDataTrigger
                val send = when (it.getSendTime()) {
                    ALWAYS -> true
                    USING -> player.isUsingSkill(it)
                    EQUIPPED -> it in player.equippedSkills
                    else -> false
                }
                if (send) ClientPlayNetworking.send(SendPlayerDataC2SPacket(it, it.write(player, NbtCompound())))
            }
    }

    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        var multiplier = 1.0f
        player.equippedSkills
            .filterIsInstance<ItemMaxUseTimeTrigger>()
            .forEach { multiplier += it.getItemMaxUseTimeMultiplier(player, stack) }
        return multiplier.coerceAtLeast(0f)
    }

    fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        player.equippedSkills
            .filterIsInstance<StatusEffectTrigger>()
            .map { it.cannotHaveStatusEffect(player, effect) }
            .any { it }
}