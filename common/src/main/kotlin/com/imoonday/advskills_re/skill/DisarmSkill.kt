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

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_DISARM_SOUND, DEFAULT_DISARM_SOUND)
            .addParameter(
                name = PARAM_SUCCESS_CHANCE,
                baseValue = DEFAULT_SUCCESS_CHANCE,
                enhancementId = ENHANCEMENT_SUCCESS_CHANCE,
                value = 0.05,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DISARM_DURATION,
                baseValue = DEFAULT_DISARM_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DROP_CHANCE,
                baseValue = DEFAULT_DROP_CHANCE,
                enhancementId = ENHANCEMENT_DROP_CHANCE,
                value = 0.01,
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

        val probability = getFloatParam(PARAM_SUCCESS_CHANCE, player, DEFAULT_SUCCESS_CHANCE)
        if (random.nextFloat() < probability) {
            val duration = getIntParam(PARAM_DISARM_DURATION, player, DEFAULT_DISARM_DURATION)
            target.addStatusEffect(StatusEffectInstance(ModEffects.DISARM.get(), duration))

            player.sendMessage(translate("skill.disarm.success"), true)
            player.playSoundFromParam(PARAM_DISARM_SOUND, DEFAULT_DISARM_SOUND.get())
            (target as? PlayerEntity)?.sendMessage(translate("skill.disarm.disarmed"), true)

            val dropChance = getFloatParam(PARAM_DROP_CHANCE, player, DEFAULT_DROP_CHANCE)
            if (random.nextFloat() < dropChance) {
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

    companion object {

        // Default Values
        private const val DEFAULT_SUCCESS_CHANCE = 0.45f
        private const val DEFAULT_DISARM_DURATION = 5 * 20
        private const val DEFAULT_DROP_CHANCE = 0.01f
        private val DEFAULT_DISARM_SOUND = ModSounds.DISARM

        // Parameter Names
        private const val PARAM_DISARM_SOUND = "disarm_sound"  // 缴械音效
        private const val PARAM_SUCCESS_CHANCE = "success_chance"  // 成功概率
        private const val PARAM_DISARM_DURATION = "disarm_duration"  // 缴械时长
        private const val PARAM_DROP_CHANCE = "drop_chance"  // 掉落概率

        // Enhancement IDs
        private const val ENHANCEMENT_SUCCESS_CHANCE = "success_chance"  // 对应成功概率
        private const val ENHANCEMENT_DURATION = "duration"  // 对应缴械时长
        private const val ENHANCEMENT_DROP_CHANCE = "drop_chance"  // 对应掉落概率
    }
}