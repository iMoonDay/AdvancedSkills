package com.imoonday.triggers

import com.imoonday.components.*
import com.imoonday.init.ModComponents
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillSlot
import net.minecraft.block.BlockState
import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object SkillTriggerHandler {

    fun onLanding(player: ServerPlayerEntity, height: Float) =
        player.equippedSkills
            .filterIsInstance<LandingTrigger>()
            .forEach { it.onLanding(player, height) }

    fun onDamaged(amount: Float, source: DamageSource, entity: LivingEntity, attacker: ServerPlayerEntity): Float {
        var newAmount = amount
        attacker.equippedSkills
            .filterIsInstance<DamageTrigger>()
            .forEach { newAmount = it.onDamaged(newAmount, source, entity, attacker) }
        return newAmount
    }


    fun onPlayerDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        var newAmount = amount
        player.equippedSkills
            .filterIsInstance<PlayerDamageTrigger>()
            .forEach { newAmount = it.onDamaged(newAmount, source, player, attacker) }
        return newAmount
    }

    fun onAttack(amount: Float, source: DamageSource, attacker: ServerPlayerEntity, entity: LivingEntity): Float {
        var newAmount = amount
        attacker.equippedSkills
            .filterIsInstance<AttackTrigger>()
            .forEach { newAmount = it.onAttack(newAmount, source, attacker, entity) }
        return newAmount
    }

    fun tick(player: ServerPlayerEntity) = player.run {
        cooldownTick(this)
        usingTick(this)
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
            .any { it.isClimbing(player) }

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

    fun getHeartType(player: PlayerEntity): HeartType? =
        player.equippedSkills
            .filterIsInstance<HeartTypeTrigger>()
            .mapNotNull { it.getHeartType(player) }
            .maxByOrNull { it.second }?.first

    private fun cooldownTick(player: ServerPlayerEntity) {
        val skills = player.getComponent(ModComponents.SKILLS).skills
        skills.filterValues { it > 0 }.forEach { skills[it.key] = it.value - 1 }
        ModComponents.SKILLS.sync(player)
    }

    private fun usingTick(player: ServerPlayerEntity) = player.updateSkillUsedTime()
}