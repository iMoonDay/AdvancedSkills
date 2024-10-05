package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.trigger.*
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

class LastDitchEffortSkill : Skill(
    id = "last_ditch_effort",
    types = listOf(SkillType.PASSIVE),
    cooldown = 180,
    rarity = Rarity.SUPERB,
    sound = ModSounds.HEAL
), DamageTrigger, AutoStopTrigger, AttackTrigger, AttributeTrigger, AutoTrigger, DeathTrigger {

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

    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float = if (!player.isUsing()) amount else amount + 1

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        if (!player.isCooling() && !player.isUsing() && !player.isDead && (player.health / player.maxHealth) < 0.3f) {
            player.health = player.maxHealth * 0.5f
            playSoundFrom(player)
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

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        player.startCooling()
        return true
    }
}