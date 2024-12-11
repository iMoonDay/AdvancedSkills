package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.server.network.*

class DisarmSkill : Skill(
    id = "disarm",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.CHANCE, SkillEnhancements.STATUS_EFFECT_DURATION)
), PostAttackTrigger, PersistentTrigger, DeathTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (!player.isUsing()) return

        val chance = player.getEnhancementLvl(SkillEnhancements.CHANCE) * 0.05f
        val random = player.random
        if (random.nextFloat() < 0.45f + chance) {
            val duration = getEnhancedValue(player, SkillEnhancements.STATUS_EFFECT_DURATION, 5 * 20)
            target.addStatusEffect(StatusEffectInstance(ModEffects.DISARM.get(), duration))
            player.sendMessage(translate("skill.disarm.success"), true)
            player.playSound(ModSounds.DISARM.get())
            (target as? PlayerEntity)?.sendMessage(translate("skill.disarm.disarmed"), true)
            val dropChance = player.getEnhancementLvl(SkillEnhancements.CHANCE) * 0.01f
            if (random.nextFloat() < 0.01f + dropChance) {
                if (target is ServerPlayerEntity)
                    target.dropSelectedItem(true)
                else if (target.dropStack(target.mainHandStack) != null) {
                    target.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY)
                } else if (target.dropStack(target.offHandStack) != null) {
                    target.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY)
                }
            }
        } else {
            player.sendMessage(translate("skill.disarm.failed"), true)
        }

        player.stopAndCooldown()
    }

    override fun onDeath(player: ServerPlayerEntity, source: DamageSource) {
        super.onDeath(player, source)
        if (player.isUsing()) {
            player.startCooling()
        }
    }
}