package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.item.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import net.minecraft.world.*

object SkillTriggerHandler {

    @JvmStatic
    fun onLanding(player: ServerPlayerEntity, height: Float) =
        player.getTriggers<LandingTrigger>()
            .forEach { it.onLanding(player, height) }

    @JvmStatic
    fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        var newAmount = amount
        player.getTriggers<DamageTrigger>()
            .forEach { newAmount = it.onDamaged(newAmount, source, player, attacker) }
        return newAmount
    }

    @JvmStatic
    fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.getTriggers<DamageTrigger>()
        .map { it.ignoreDamage(amount, source, player, attacker) }
        .any { it }

    @JvmStatic
    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float {
        var newAmount = amount
        player.getTriggers<AttackTrigger>()
            .forEach { newAmount = it.onAttack(newAmount, source, player, target) }
        return newAmount
    }

    @JvmStatic
    fun serverTick(player: ServerPlayerEntity) = player.run {
        getTriggers<AutoStartTrigger>()
            .filterNot { player.isUsing(it.getAsSkill()) }
            .forEach { onStart(player, it.getAsSkill()) }
        getTriggers<AutoTrigger>().forEach { it.tick(this) }
        player.getTriggers<TickTrigger>()
            .forEach { it.serverTick(player, player.getUsedTime(it.getAsSkill())) }
    }

    @JvmStatic
    fun playerTick(player: PlayerEntity) {
        if (player is ServerPlayerEntity) serverTick(player)
        else player.getTriggers<TickTrigger>()
            .forEach { it.clientTick(player, player.getUsedTime(it.getAsSkill())) }
    }

    @JvmStatic
    fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        var newAmount = amount
        player.getTriggers<FallTrigger>()
            .forEach { newAmount = it.onFall(newAmount, player, fallDistance, damageMultiplier) }
        return newAmount
    }

    @JvmStatic
    fun onStart(player: ServerPlayerEntity, skill: Skill) {
        if (skill is AutoStartTrigger) {
            skill.onStart(player)
            player.startUsing(skill)
        }
    }

    @JvmStatic
    fun postMine(world: World, block: BlockState, pos: BlockPos, miner: PlayerEntity, item: ItemStack) {
        miner.getTriggers<MiningTrigger>()
            .forEach { it.postMine(world, block, pos, miner, item) }
    }

    @JvmStatic
    fun postHit(target: LivingEntity, attacker: PlayerEntity, item: ItemStack) {
        attacker.getTriggers<HitTrigger>()
            .forEach { it.postHit(target, attacker, item) }
    }

    @JvmStatic
    fun allowClimbing(player: PlayerEntity): Boolean =
        player.getTriggers<ClimbingTrigger>()
            .map { it.isClimbing(player) }
            .any { it }

    @JvmStatic
    fun onEquipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? EquipTrigger)?.onEquipped(player, slot) ?: true

    @JvmStatic
    fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? EquipTrigger)?.postEquipped(player, slot)

    @JvmStatic
    fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? UnequipTrigger)?.onUnequipped(player, slot) ?: true

    @JvmStatic
    fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot, skill: Skill) =
        (skill as? UnequipTrigger)?.postUnequipped(player, slot)

    @JvmStatic
    fun allowWalkOnFluid(player: PlayerEntity, fluidState: FluidState) =
        player.getTriggers<WalkOnFluidTrigger>()
            .map { it.canWalkOnFluid(player, fluidState) }
            .any { it }

    @JvmStatic
    fun getStepHeight(player: PlayerEntity): Float? =
        player.getTriggers<StepHeightTrigger>()
            .mapNotNull { it.getStepHeight(player) }
            .maxOrNull()

    @JvmStatic
    fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double {
        var newSpeed = speed
        player.getTriggers<FluidMovementTrigger>()
            .forEach { newSpeed = it.getMovementInFluid(player, tag, newSpeed) }
        return newSpeed
    }

    @JvmStatic
    fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean =
        player.getTriggers<FluidMovementTrigger>()
            .map { it.ignoreFluid(player, tag) }
            .any { it }

    @JvmStatic
    fun canBreatheInWater(player: PlayerEntity): Boolean {
        return player.getTriggers<BreatheInWaterTrigger>()
            .map { it.canBreatheInWater(player) }
            .any { it }
    }

    @JvmStatic
    fun isInvisible(player: PlayerEntity): Boolean =
        player.getTriggers<InvisibilityTrigger>()
            .map { it.isInvisible(player) }
            .any { it }

    @JvmStatic
    fun isInvisibleTo(player: PlayerEntity, otherPlayer: PlayerEntity): Boolean =
        player.getTriggers<InvisibilityTrigger>()
            .map { it.isInvisibleTo(player, otherPlayer) }
            .any { it }

    @JvmStatic
    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        var multiplier = 1.0f
        player.getTriggers<ItemMaxUseTimeTrigger>()
            .forEach { multiplier += it.getItemMaxUseTimeMultiplier(player, stack) }
        return multiplier.coerceAtLeast(0f)
    }

    @JvmStatic
    fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        player.getTriggers<StatusEffectTrigger>()
            .map { it.cannotHaveStatusEffect(player, effect) }
            .any { it }

    @JvmStatic
    fun shouldFlipUpsideDown(player: PlayerEntity): Boolean =
        player.getTriggers<FlipUpsideDownTrigger>()
            .map { it.shouldFlipUpsideDown(player) }
            .any { it }

    @JvmStatic
    fun getEyeHeight(player: PlayerEntity, original: Float, pose: EntityPose, dimensions: EntityDimensions): Float {
        var height = original
        player.getTriggers<EyeHeightTrigger>()
            .forEach { height = it.getEyeHeight(player, height, pose, dimensions) }
        return height
    }

    @JvmStatic
    fun postStop(player: PlayerEntity) {
        player.getTriggers<StopTrigger>()
            .forEach { it.postStop(player) }
    }

    @JvmStatic
    fun shouldInvertJump(player: PlayerEntity): Boolean =
        player.getTriggers<InvertInputTrigger>()
            .map { it.shouldInvertJump(player) }.any { it }

    @JvmStatic
    fun shouldInvertSneak(player: PlayerEntity): Boolean =
        player.getTriggers<InvertInputTrigger>()
            .map { it.shouldInvertSneak(player) }.any { it }

    @JvmStatic
    fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) =
        player.getTriggers<PostDamagedTrigger>()
            .forEach { it.postDamaged(amount, source, player, attacker) }

    @JvmStatic
    fun postSweepAttack(player: PlayerEntity, target: LivingEntity) =
        player.getTriggers<PostAttackTrigger>()
            .forEach { it.postSweepAttack(player, target) }

    @JvmStatic
    fun hasNightVision(player: PlayerEntity): Boolean =
        player.getTriggers<NightVisionTrigger>()
            .map { it.hasNightVision(player) }
            .any { it }

    @JvmStatic
    fun isTaunter(player: PlayerEntity): Boolean =
        player.getTriggers<TauntTrigger>()
            .map { it.isTaunting(player) }
            .any { it }

    @JvmStatic
    fun isDisguising(player: PlayerEntity): Boolean =
        player.getTriggers<DisguiseTrigger>()
            .map { it.isDisguising(player) }
            .any { it }

    @JvmStatic
    fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) =
        player.getTriggers<PostAttackedTrigger>()
            .forEach { it.postAttacked(source, player, attacker) }

    @JvmStatic
    fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) =
        player.getTriggers<PostAttackTrigger>()
            .forEach { it.postAttack(source, player, target) }
}