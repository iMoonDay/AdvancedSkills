package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.startCooling
import com.imoonday.components.stopUsingSkill
import com.imoonday.init.ModEffects
import com.imoonday.init.ModSounds
import com.imoonday.triggers.AttackTrigger
import com.imoonday.triggers.PersistentTrigger
import com.imoonday.triggers.RespawnTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.translate
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
    rarity = Rarity.VERY_RARE,
), AttackTrigger, PersistentTrigger, RespawnTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(false)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        attacker: ServerPlayerEntity,
        entity: LivingEntity,
    ): Float {
        if (!attacker.isUsingSkill(this)) return amount
        if (Random.nextFloat() <= 0.45f) {
            entity.addStatusEffect(StatusEffectInstance(ModEffects.DISARM, 20 * 5, 0))
            attacker.sendMessage(translate("skill", "disarm.success"), true)
            entity.world.playSound(null, attacker.blockPos, ModSounds.DISARM, SoundCategory.PLAYERS)
            (entity as? PlayerEntity)?.sendMessage(translate("skill", "disarm.disarmed"), true)
            if (Random.nextFloat() <= 0.01f) {
                if (entity is ServerPlayerEntity)
                    entity.dropSelectedItem(true)
                else if (entity.dropStack(entity.mainHandStack) != null) {
                    entity.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY)
                } else if (entity.dropStack(entity.offHandStack) != null) {
                    entity.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY)
                }
            }
        } else {
            attacker.sendMessage(translate("skill", "disarm.failed"), true)
        }

        attacker.stopUsingSkill(this)
        attacker.startCooling(this)
        return amount
    }

    override fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        if (oldPlayer.isUsingSkill(this)) newPlayer.startCooling(this)
    }
}