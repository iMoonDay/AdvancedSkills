package com.imoonday.skill

import com.imoonday.entity.*
import com.imoonday.init.*
import com.imoonday.trigger.*
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.equippedSkills
import com.imoonday.util.playSound
import com.imoonday.util.resetUsedTime
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.mob.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.projectile.*
import net.minecraft.entity.projectile.thrown.*
import net.minecraft.potion.*
import net.minecraft.server.network.*

class DangerPerceptionSkill : Skill(
    id = "danger_perception",
    types = listOf(SkillType.PASSIVE),
    cooldown = 12,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, AttributeTrigger, DamageTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override val persistTime: Int = 20 * 2

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Danger Perception"),
            "Danger Perception",
            0.3,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AttributeTrigger>.postUnequipped(player, slot)
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isCreative && !player.isSpectator && (player.isUsing() || !player.isCooling())) {
            val hasDanger = player.world
                .getOtherEntities(player, player.boundingBox.expand(3.0)) { dangerTest(player, it) }
                .isNotEmpty()
            if (hasDanger) {
                if (player.isUsing()) player.resetUsedTime(this) else start(player)
            }
        }
        super.serverTick(player, usedTime)
    }

    override fun onStop(player: ServerPlayerEntity) {
        player.removeAttributes()
        super.onStop(player)
    }

    private fun dangerTest(player: ServerPlayerEntity, entity: Entity): Boolean = when {
        safetyList.any { it(player, entity) } -> false
        dangerList.any { it(player, entity) } -> true
        else -> false
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing() || player.isCooling()) return amount
        if (source.source != null || source.attacker != null) {
            if (source.source != null && source.source == player) return amount
            if (source.attacker != null && source.attacker == player) return amount
            start(player)
        }
        return amount
    }

    private fun start(player: ServerPlayerEntity) {
        player.playSound(ModSounds.DASH.get())
        player.startUsing()
        player.startCooling()
        player.addAttributes()
    }

    object DangerTestEvents {

        fun addDangerCondition(condition: (ServerPlayerEntity, Entity) -> Boolean) {
            dangerList.add(condition)
        }

        fun addSafetyCondition(condition: (ServerPlayerEntity, Entity) -> Boolean) {
            safetyList.add(condition)
        }
    }

    companion object {

        private val dangerList: MutableList<(ServerPlayerEntity, Entity) -> Boolean> = mutableListOf()
        private val safetyList: MutableList<(ServerPlayerEntity, Entity) -> Boolean> = mutableListOf()

        init {
            DangerTestEvents.addDangerCondition { player, entity ->
                entity.let {
                    when {
                        it is ProjectileEntity && !it.isOnGround && it.owner != player -> true
                        it is Monster && it.isAlive -> true
                        it is HostileEntity && it.isAlive && it.target == player -> true
                        it is PotionEntity &&
                            (PotionUtil.getPotion(it.stack).effects
                                .any { !it.effectType.isBeneficial }
                                || PotionUtil.getPotionEffects(it.stack)
                                .any { !it.effectType.isBeneficial })
                        -> true

                        it is TntEntity -> true
                        it is PufferfishEntity && it.puffState > 0 -> true
                        it is FallingBlockEntity && it.blockX == player.blockX && it.blockY >= player.blockY && it.blockZ == player.blockZ -> true
                        it is ServerPlayerEntity && it.equippedSkills.any { skill -> skill.isDangerous(it) } -> true
                        else -> false
                    }
                }
            }
            DangerTestEvents.addSafetyCondition { player, entity ->
                entity is Servant && entity.owner == player
            }
        }
    }
}