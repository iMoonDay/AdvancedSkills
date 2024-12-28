package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.mob.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.*
import net.minecraft.entity.projectile.*
import net.minecraft.entity.projectile.thrown.*
import net.minecraft.particle.*
import net.minecraft.potion.*
import net.minecraft.server.network.*

class DangerPerceptionSkill : Skill(
    id = "danger_perception",
    types = listOf(SkillType.PASSIVE),
    cooldown = 12,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.MOVEMENT_SPEED, SkillEnhancements.RANGE)
), AutoStopTrigger, AttributeTrigger, DamageTrigger, UsingRenderTrigger {

    init {
        addEnhanceableParameter(
            timeParamName,
            2 * 20,
            "time",
            0.2f,
            Enhancement.Operation.MULTIPLY,
            5
        ) { (it * 100).toInt() }

        addEnhancementTooltipWithArg(SkillEnhancements.MOVEMENT_SPEED) { it.level * 6 }
        addEnhancementTooltipWithArg(SkillEnhancements.RANGE) { it.level * 0.4f }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Danger Perception"),
            "Danger Perception",
            0.3 + player.getEnhancementLvl(SkillEnhancements.MOVEMENT_SPEED) * 0.06,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AttributeTrigger>.postUnequipped(player, slot)
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isCreative && !player.isSpectator && !player.abilities.invulnerable && (player.isUsing() || !player.isCooling())) {
            val range = player.getEnhancementLvl(SkillEnhancements.RANGE) * 0.4
            val hasDanger = player.world
                .getOtherEntities(player, player.boundingBox.expand(3.0 + range)) { dangerTest(player, it) }
                .isNotEmpty()
            if (hasDanger) {
                if (player.isUsing()) {
                    player.resetUsedTime(this)
                } else {
                    start(player)
                }
            }
        }
        super.serverTick(player, usedTime)
    }

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
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
        player.addAttributes()
        player.spawnParticles(
            ParticleTypes.CLOUD,
            false, player.pos, 15,
            0.0, 0.0, 0.0, 0.1
        )
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
                                .any { effect -> !effect.effectType.isBeneficial }
                                || PotionUtil.getPotionEffects(it.stack)
                                .any { effect -> !effect.effectType.isBeneficial })
                        -> true

                        it is TntEntity -> true
                        it is PufferfishEntity && it.puffState > 0 -> true
                        it is FallingBlockEntity && it.blockX == player.blockX && it.blockY >= player.blockY && it.blockZ == player.blockZ -> true
                        it is ServerPlayerEntity
                            && it.anyTrigger<DangerTrigger> { skill -> skill.isDangerousTo(it, player) } -> true

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