package com.imoonday.trigger

import com.imoonday.network.SendPlayerDataC2SPacket
import com.imoonday.skill.Skill
import com.imoonday.trigger.SendTime.*
import com.imoonday.util.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
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
        player.getTriggers<LandingTrigger>()
            .forEach { it.onLanding(player, height) }

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

    fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.getTriggers<DamageTrigger>()
        .map { it.ignoreDamage(amount, source, player, attacker) }
        .any { it }

    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float {
        var newAmount = amount
        player.getTriggers<AttackTrigger>()
            .forEach { newAmount = it.onAttack(newAmount, source, player, target) }
        return newAmount
    }

    fun serverTick(player: ServerPlayerEntity) = player.run {
        getTriggers<AutoStartTrigger>()
            .filterNot { player.isUsing(it.getAsSkill()) }
            .forEach { onStart(player, it.getAsSkill()) }
        getTriggers<AutoTrigger>().forEach { it.tick(this) }
    }

    fun tick(player: PlayerEntity) {
        if (player is ServerPlayerEntity) player.getTriggers<TickTrigger>()
            .forEach { it.serverTick(player, player.getUsedTime(it.getAsSkill())) }
        else if (player is ClientPlayerEntity) player.getTriggers<TickTrigger>()
            .forEach { it.clientTick(player, player.getUsedTime(it.getAsSkill())) }
    }

    fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        var newAmount = amount
        player.getTriggers<FallTrigger>()
            .forEach { newAmount = it.onFall(newAmount, player, fallDistance, damageMultiplier) }
        return newAmount
    }

    fun onStart(player: ServerPlayerEntity, skill: Skill) {
        if (skill is AutoStartTrigger) {
            skill.onStart(player)
            player.startUsing(skill)
        }
    }

    fun postMine(world: World, block: BlockState, pos: BlockPos, miner: PlayerEntity, item: ItemStack) {
        miner.getTriggers<MiningTrigger>()
            .forEach { it.postMine(world, block, pos, miner, item) }
    }

    fun postHit(target: LivingEntity, attacker: PlayerEntity, item: ItemStack) {
        attacker.getTriggers<HitTrigger>()
            .forEach { it.postHit(target, attacker, item) }
    }

    fun allowClimbing(player: PlayerEntity): Boolean =
        player.getTriggers<ClimbingTrigger>()
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
        player.getTriggers<WalkOnFluidTrigger>()
            .map { it.canWalkOnFluid(player, fluidState) }
            .any { it }

    fun getHeartType(player: PlayerEntity): HeartType? =
        player.getTriggers<HeartTypeTrigger>()
            .mapNotNull { it.getHeartType(player) }
            .maxByOrNull { it.second }?.first

    fun getStepHeight(player: PlayerEntity): Float? =
        player.getTriggers<StepHeightTrigger>()
            .mapNotNull { it.getStepHeight(player) }
            .maxOrNull()

    fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double {
        var newSpeed = speed
        player.getTriggers<FluidMovementTrigger>()
            .forEach { newSpeed = it.getMovementInFluid(player, tag, newSpeed) }
        return newSpeed
    }

    fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean =
        player.getTriggers<FluidMovementTrigger>()
            .map { it.ignoreFluid(player, tag) }
            .any { it }

    fun canBreatheInWater(player: PlayerEntity): Boolean {
        return player.getTriggers<BreatheInWaterTrigger>()
            .map { it.canBreatheInWater(player) }
            .any { it }
    }

    fun isInvisible(player: PlayerEntity): Boolean =
        player.getTriggers<InvisibilityTrigger>()
            .map { it.isInvisible(player) }
            .any { it }

    fun isInvisibleTo(player: PlayerEntity, otherPlayer: PlayerEntity): Boolean =
        player.getTriggers<InvisibilityTrigger>()
            .map { it.isInvisibleTo(player, otherPlayer) }
            .any { it }

    fun sendPlayerData(player: ClientPlayerEntity) =
        player.getTriggers<SendPlayerDataTrigger>()
            .filter { it.getSendTime() != USE }
            .forEach {
                val skill = it.getAsSkill()
                when (it.getSendTime()) {
                    ALWAYS -> true
                    USING -> player.isUsing(skill)
                    EQUIPPED -> player.hasEquipped(skill)
                    else -> false
                }.run {
                    if (this) ClientPlayNetworking.send(SendPlayerDataC2SPacket(skill, it.write(player, NbtCompound())))
                }
            }

    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        var multiplier = 1.0f
        player.getTriggers<ItemMaxUseTimeTrigger>()
            .forEach { multiplier += it.getItemMaxUseTimeMultiplier(player, stack) }
        return multiplier.coerceAtLeast(0f)
    }

    fun cannotHaveStatusEffect(player: PlayerEntity, effect: StatusEffectInstance): Boolean =
        player.getTriggers<StatusEffectTrigger>()
            .map { it.cannotHaveStatusEffect(player, effect) }
            .any { it }

    fun isGlowing(player: ClientPlayerEntity, entity: Entity): Boolean =
        player.getTriggers<GlowingTrigger>()
            .map { it.isGlowing(player, entity) }
            .any { it }

    fun worldRender(matrixStack: MatrixStack, tickDelta: Float, client: MinecraftClient) {
        client.player?.getTriggers<WorldRenderTrigger>()
            ?.forEach { it.apply(matrixStack, tickDelta, client) }
    }

    fun shouldInvertMouse(): Pair<Boolean, Boolean> =
        clientPlayer?.getTriggers<InvertMouseTrigger>()?.run {
            map { it.shouldInvertMouseX() }.any { it } to map { it.shouldInvertMouseY() }.any { it }
        } ?: (false to false)

    fun shouldFlipUpsideDown(player: PlayerEntity): Boolean =
        player.getTriggers<FlipUpsideDownTrigger>()
            .map { it.shouldFlipUpsideDown(player) }
            .any { it }

    fun getEyeHeight(player: PlayerEntity, original: Float, pose: EntityPose, dimensions: EntityDimensions): Float {
        if (!player.skillInitialized) return original
        var height = original
        player.getTriggers<EyeHeightTrigger>()
            .forEach { height = it.getEyeHeight(player, height, pose, dimensions) }
        return height
    }

    fun postStop(player: PlayerEntity) {
        player.getTriggers<StopTrigger>()
            .forEach { it.postStop(player) }
    }

    fun shouldInvertInput(): Pair<Boolean, Boolean> =
        clientPlayer?.getTriggers<InvertInputTrigger>()?.run {
            map { it.shouldInvertHorizontalInput() }.any { it } to map { it.shouldInvertVerticalInput() }.any { it }
        } ?: (false to false)

    fun shouldInvertJump(player: PlayerEntity): Boolean =
        player.getTriggers<InvertInputTrigger>()
            .map { it.shouldInvertJump(player) }.any { it }

    fun shouldInvertSneak(player: PlayerEntity): Boolean =
        player.getTriggers<InvertInputTrigger>()
            .map { it.shouldInvertSneak(player) }.any { it }

    fun getCameraMovement(original: Float): Float {
        var movement = original
        clientPlayer?.getTriggers<CameraUpdateMovementTrigger>()
            ?.forEach { movement = it.getDelta(movement) }
        return movement
    }

    fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        player.getTriggers<PostDamagedTrigger>()
            .forEach { it.postDamaged(amount, source, player, attacker) }
    }
}