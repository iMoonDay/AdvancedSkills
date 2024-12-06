package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class LastDitchEffortSkill : Skill(
    id = "last_ditch_effort",
    types = listOf(SkillType.PASSIVE),
    cooldown = 180,
    rarity = SkillRarity.SUPERB,
    sound = ModSounds.HEAL
), DamageTrigger, AutoStopTrigger, AttackTrigger,
    AttributeTrigger, AutoTrigger, DeathTrigger {

    override val persistTime: Int = 15 * 20

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Last Ditch Effort"),
            "Last Ditch Effort",
            0.4,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
        super<AttributeTrigger>.postUnequipped(player, slot)
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float = if (!player.isUsing()) amount else amount + 1

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        if (player.isReady() && !player.isDead && (player.health / player.maxHealth) < 0.3f) {
            player.health = player.maxHealth * 0.5f
            player.playSkillSound()
            player.addAttributes()
            true
        } else false

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount else amount + 1

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        player.removeAttributes()
        super.onStop(player)
    }

    override fun onDeath(player: ServerPlayerEntity, source: DamageSource) {
        super.onDeath(player, source)
        if (player.isUsing()) {
            player.startCooling()
        }
    }
}