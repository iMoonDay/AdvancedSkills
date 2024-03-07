package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import net.minecraft.client.gui.hud.InGameHud.HeartType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class DyingCounterattackSkill : Skill(
    id = "dying_counterattack",
    types = arrayOf(SkillType.PASSIVE),
    cooldown = 180,
    rarity = Rarity.EPIC,
), DeathTrigger, PersistentTrigger, AttackTrigger, TickTrigger, UnequipTrigger, HeartTypeTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || player.isUsing()) return true
        player.health = player.maxHealth
        player.startUsing()
        player.playSound(SoundEvents.ITEM_TOTEM_USE)
        return false
    }

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float {
        if (!player.isUsing()) return amount
        if (amount > 0) player.heal(amount / 10)
        return amount
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (usedTime % 20 == 0) player.damage(
            player.damageSources.wither(),
            2.0f * (usedTime / 200 + if (usedTime % 200 == 0) 0 else 1)
        )
        if (player.isDead) player.startCooling()
    }

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean =
        !player.isUsing()

    override fun getHeartType(player: PlayerEntity): Pair<HeartType, Int>? =
        if (player.isUsing()) HeartType.WITHERED to 100 else null
}
