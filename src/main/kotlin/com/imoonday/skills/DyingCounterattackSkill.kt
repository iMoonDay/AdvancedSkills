package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.startCooling
import com.imoonday.components.startUsingSkill
import com.imoonday.trigger.*
import com.imoonday.utils.*
import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class DyingCounterattackSkill : Skill(
    id = "dying_counterattack",
    types = arrayOf(SkillType.PASSIVE),
    cooldown = 180,
    rarity = Rarity.EPIC,
), DeathTrigger, PersistentTrigger, AttackTrigger, TickTrigger, UnequipTrigger, HeartTypeTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    private val skill
        get() = this

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || player.isUsingSkill(skill)) return true
        player.health = player.maxHealth
        player.startUsingSkill(skill)
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS)
        return false
    }

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        attacker: ServerPlayerEntity,
        entity: LivingEntity,
    ): Float {
        if (!attacker.isUsingSkill(this)) return amount
        if (amount > 0) attacker.heal(amount / 10)
        return amount
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsingSkill(this)) return
        if (usedTime % 20 == 0) player.damage(
            player.damageSources.wither(),
            2.0f * (usedTime / 200 + if (usedTime % 200 == 0) 0 else 1)
        )
        if (player.isDead) player.startCooling(skill)
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean =
        !player.isUsingSkill(skill)

    override fun getHeartType(player: PlayerEntity): Pair<HeartType, Int>? =
        if (player.isUsingSkill(this)) HeartType.WITHERED to 100 else null
}
