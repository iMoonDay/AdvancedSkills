package com.imoonday.trigger

import com.imoonday.component.*
import com.imoonday.init.ModComponents
import com.imoonday.skill.Skill
import com.imoonday.util.SkillSlot
import net.minecraft.block.BlockState
import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
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

    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float {
        var newAmount = amount
        player.equippedSkills
            .filterIsInstance<AttackTrigger>()
            .forEach { newAmount = it.onAttack(newAmount, source, player, target) }
        return newAmount
    }

    fun tick(player: ServerPlayerEntity) = player.run {
        cooldownTick(this)
        updateSkillUsedTime()
        equippedSkills.filter { it is AutoStartTrigger }
            .filterNot { it in usingSkills }
            .forEach { onStart(player, it) }
        equippedSkills.filterNot { it in usingSkills }
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

    fun syncVelocity(player: ClientPlayerEntity) {
        player.equippedSkills
            .filterIsInstance<VelocitySyncTrigger>()
            .forEach { it.syncVelocity(player) }
    }

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

    private fun cooldownTick(player: ServerPlayerEntity) {
        val skills = player.getComponent(ModComponents.SKILLS).skills
        skills.filterValues { it > 0 }.forEach { skills[it.key] = it.value - 1 }
        ModComponents.SKILLS.sync(player)
    }
}