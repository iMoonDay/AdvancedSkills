package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.translate
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import kotlin.random.*

class DisarmSkill : Skill(
    id = "disarm",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB,
), AttackTrigger, PersistentTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(false)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float {
        if (!player.isUsing()) return amount
        if (Random.nextFloat() <= 0.45f) {
            target.addStatusEffect(StatusEffectInstance(ModEffects.DISARM.get(), 20 * 5, 0))
            player.sendMessage(translate("skill", "disarm.success"), true)
            target.world.playSound(null, player.blockPos, ModSounds.DISARM.get(), SoundCategory.PLAYERS)
            (target as? PlayerEntity)?.sendMessage(translate("skill", "disarm.disarmed"), true)
            if (Random.nextFloat() <= 0.01f) {
                if (target is ServerPlayerEntity)
                    target.dropSelectedItem(true)
                else if (target.dropStack(target.mainHandStack) != null) {
                    target.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY)
                } else if (target.dropStack(target.offHandStack) != null) {
                    target.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY)
                }
            }
        } else {
            player.sendMessage(translate("skill", "disarm.failed"), true)
        }

        player.stopUsing()
        player.startCooling()
        return amount
    }

    override fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        if (oldPlayer.isUsing()) newPlayer.startCooling()
    }
}