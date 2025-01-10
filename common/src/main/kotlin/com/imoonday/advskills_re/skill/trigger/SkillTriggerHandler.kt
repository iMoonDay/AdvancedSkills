package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
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
        player.forEachTrigger<LandingTrigger> { it.onLanding(player, height) }

    @JvmStatic
    fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        var newAmount = amount
        player.forEachTrigger<DamageTrigger> { newAmount = it.onDamaged(newAmount, source, player, attacker) }
        return newAmount
    }

    @JvmStatic
    fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.anyTrigger<DamageTrigger> { it.ignoreDamage(amount, source, player, attacker) }

    @JvmStatic
    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float {
        var newAmount = amount
        player.forEachTrigger<AttackTrigger> {
            newAmount = it.onAttack(newAmount, source, player, target)
        }
        return newAmount
    }

    @JvmStatic
    fun serverTick(player: ServerPlayerEntity) = player.run {
        forEachTrigger<AutoStartTrigger>({ !isUsing(it.getAsSkill()) }) { onStart(this, it.getAsSkill()) }
        forEachTrigger<AutoTrigger> { it.tick(this) }
        forEachTrigger<TickTrigger> { it.serverTick(this, getUsedTime(it.getAsSkill())) }
    }

    @JvmStatic
    fun playerTick(player: PlayerEntity) {
        if (player is ServerPlayerEntity) serverTick(player)
        else player.forEachTrigger<TickTrigger> { it.clientTick(player, player.getUsedTime(it.getAsSkill())) }
    }

    @JvmStatic
    fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        var newAmount = amount
        player.forEachTrigger<FallTrigger> { newAmount = it.onFall(newAmount, player, fallDistance, damageMultiplier) }
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
    fun postMine(world: World, block: BlockState, pos: BlockPos, miner: PlayerEntity, item: ItemStack) =
        miner.forEachTrigger<MiningTrigger> { it.postMine(world, block, pos, miner, item) }

    @JvmStatic
    fun postHit(target: LivingEntity, attacker: PlayerEntity, item: ItemStack) =
        attacker.forEachTrigger<HitTrigger> { it.postHit(target, attacker, item) }

    @JvmStatic
    fun allowClimbing(player: PlayerEntity): Boolean =
        player.anyTrigger<ClimbingTrigger> { it.isClimbing(player) }

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
        player.anyTrigger<WalkOnFluidTrigger> { it.canWalkOnFluid(player, fluidState) }

    @JvmStatic
    fun getStepHeight(player: PlayerEntity): Float? =
        player.getTriggers<StepHeightTrigger>()
            .mapNotNull { it.getStepHeight(player) }
            .maxOrNull()

    @JvmStatic
    fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double {
        var newSpeed = speed
        player.forEachTrigger<FluidMovementTrigger> { newSpeed = it.getMovementInFluid(player, tag, newSpeed) }
        return newSpeed
    }

    @JvmStatic
    fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean =
        player.anyTrigger<FluidMovementTrigger> { it.ignoreFluid(player, tag) }

    @JvmStatic
    fun canBreatheInWater(player: PlayerEntity): Boolean =
        player.anyTrigger<BreatheInWaterTrigger> { it.canBreatheInWater(player) }

    @JvmStatic
    fun isInvisible(player: PlayerEntity): Boolean =
        player.anyTrigger<InvisibilityTrigger> { it.isInvisible(player) }

    @JvmStatic
    fun isInvisibleTo(player: PlayerEntity, otherPlayer: PlayerEntity): Boolean =
        player.anyTrigger<InvisibilityTrigger> { it.isInvisibleTo(player, otherPlayer) }

    @JvmStatic
    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        var multiplier = 1.0f
        player.forEachTrigger<ItemMaxUseTimeTrigger> { multiplier += it.getItemMaxUseTimeMultiplier(player, stack) }
        return multiplier.coerceAtLeast(0f)
    }

    @JvmStatic
    fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        player.anyTrigger<StatusEffectTrigger> { it.cannotHaveStatusEffect(player, effect) }

    @JvmStatic
    fun shouldFlipUpsideDown(player: PlayerEntity): Boolean =
        player.anyTrigger<FlipUpsideDownTrigger> { it.shouldFlipUpsideDown(player) }

    @JvmStatic
    fun getEyeHeight(player: PlayerEntity, original: Float, pose: EntityPose, dimensions: EntityDimensions): Float {
        var height = original
        player.forEachTrigger<EyeHeightTrigger> { height = it.getEyeHeight(player, height, pose, dimensions) }
        return height
    }

    @JvmStatic
    fun postStop(player: PlayerEntity) = player.forEachTrigger<StopTrigger> { it.postStop(player) }

    @JvmStatic
    fun shouldInvertJump(player: PlayerEntity): Boolean =
        player.getTriggers<InvertInputTrigger>()
            .map { it.shouldInvertJump(player) }.any { it }

    @JvmStatic
    fun shouldInvertSneak(player: PlayerEntity): Boolean =
        player.anyTrigger<InvertInputTrigger> { it.shouldInvertSneak(player) }

    @JvmStatic
    fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) =
        player.forEachTrigger<PostDamagedTrigger> { it.postDamaged(amount, source, player, attacker) }

    @JvmStatic
    fun postSweepAttack(player: PlayerEntity, target: LivingEntity) =
        player.forEachTrigger<PostAttackTrigger> { it.postSweepAttack(player, target) }

    @JvmStatic
    fun hasNightVision(player: PlayerEntity): Boolean =
        player.anyTrigger<NightVisionTrigger> { it.hasNightVision(player) }

    @JvmStatic
    fun isTaunter(player: PlayerEntity): Boolean =
        player.anyTrigger<TauntTrigger> { it.isTaunting(player) }

    @JvmStatic
    fun isDisguising(player: PlayerEntity): Boolean =
        player.anyTrigger<DisguiseTrigger> { it.isDisguising(player) }

    @JvmStatic
    fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) =
        player.forEachTrigger<PostAttackedTrigger> { it.postAttacked(source, player, attacker) }

    @JvmStatic
    fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) =
        player.forEachTrigger<PostAttackTrigger> { it.postAttack(source, player, target) }

    @JvmStatic
    fun ignoreLava(player: PlayerEntity): Boolean = player.anyTrigger<LavaTrigger> { it.ignoreLava(player) }

    @JvmStatic
    fun shouldHaveStatusEffect(player: PlayerEntity, effect: StatusEffect): Boolean =
        player.anyTrigger<StatusEffectTrigger> { it.shouldHaveStatusEffect(player, effect) }

    @JvmStatic
    fun shouldIgnoreGravity(player: PlayerEntity): Boolean =
        player.anyTrigger<GravityTrigger> { it.shouldIgnoreGravity(player) }

    @JvmStatic
    fun onFoodLevelChange(player: PlayerEntity, level: Int): Int {
        var newLevel = level
        player.forEachTrigger<HungerTrigger> { newLevel = it.onFoodLevelChange(player, newLevel) }
        return newLevel
    }

    @JvmStatic
    fun isSaveMoving(player: PlayerEntity): Boolean =
        player.anyTrigger<SaveMovingTrigger> { it.isSaveMoving(player) }
}