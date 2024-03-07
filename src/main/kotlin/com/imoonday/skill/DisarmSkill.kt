package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.component.startCooling
import com.imoonday.component.stopUsingSkill
import com.imoonday.init.ModEffects
import com.imoonday.init.ModSounds
import com.imoonday.trigger.AttackTrigger
import com.imoonday.trigger.PersistentTrigger
import com.imoonday.trigger.RespawnTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.translate
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import kotlin.random.Random

class DisarmSkill : Skill(
    id = "disarm",
    types = arrayOf(SkillType.ENHANCEMENT),
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
            target.addStatusEffect(StatusEffectInstance(ModEffects.DISARM, 20 * 5, 0))
            player.sendMessage(translate("skill", "disarm.success"), true)
            target.world.playSound(null, player.blockPos, ModSounds.DISARM, SoundCategory.PLAYERS)
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