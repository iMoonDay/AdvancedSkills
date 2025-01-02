package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
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
    Settings(
        id = "disarm",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), PostAttackTrigger, PersistentTrigger, DeathTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("disarm_sound", ModSounds.DISARM)
            .addParameter(
                name = "disarm_probability",
                baseValue = 0.45f,
                enhancementId = "success_probability",
                value = 0.05f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "disarm_duration",
                baseValue = 5 * 20,
                enhancementId = "duration",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "loot_probability",
                baseValue = 0.01f,
                enhancementId = "loot_probability",
                value = 0.01f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (!player.isUsing()) return

        val random = player.random

        val probability = getFloatParam("disarm_probability", player, 0.45f)
        if (random.nextFloat() < probability) {
            val duration = getIntParam("disarm_duration", player, 5 * 20)
            target.addStatusEffect(StatusEffectInstance(ModEffects.DISARM.get(), duration))

            player.sendMessage(translate("skill.disarm.success"), true)
            player.playSoundFromParam("disarm_sound", ModSounds.DISARM.get())
            (target as? PlayerEntity)?.sendMessage(translate("skill.disarm.disarmed"), true)

            val lootProbability = getFloatParam("loot_probability", player, 0.01f)
            if (random.nextFloat() < lootProbability) {
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