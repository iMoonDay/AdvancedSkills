package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class SwordSoulGuardingSkill : Skill(
    Settings(
        id = "sword_soul_guarding",
        types = listOf(SkillType.SUMMON, SkillType.ATTACK),
        cooldown = 30,
        rarity = SkillRarity.LEGENDARY
    )
), PostAttackTrigger, PostAttackedTrigger, AutoStopTrigger, UsingRenderTrigger, DangerTrigger {

    init {
        settings
            .addParameter(PARAM_SWORD_SOUND, DEFAULT_SWORD_SOUND)
            .addParameter(
                name = PARAM_GUARD_DURATION,
                baseValue = DEFAULT_GUARD_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_TRIGGER_CHANCE,
                baseValue = DEFAULT_TRIGGER_CHANCE,
                enhancementId = ENHANCEMENT_CHANCE,
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_SWORD_COUNT,
                baseValue = DEFAULT_SWORD_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_SPAWN_INTERVAL,
                baseValue = DEFAULT_SPAWN_INTERVAL,
                enhancementId = ENHANCEMENT_INTERVAL,
                value = -2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        val chance = getFloatParam(PARAM_TRIGGER_CHANCE, player, DEFAULT_TRIGGER_CHANCE)
        if (player.isUsing() && source.source !is EnchantedSwordEntity && player.random.nextFloat() < chance) {
            spawnSword(player, target)
        }
    }

    override fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postAttacked(source, player, attacker)
        if (player.isUsing()) {
            attacker?.let {
                spawnSword(player, attacker)
            }
        }
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    private fun spawnSword(player: ServerPlayerEntity, target: LivingEntity) {
        if (target.isRemoved || target.isDead) return
        val swordCount = getIntParam(PARAM_SWORD_COUNT, player, DEFAULT_SWORD_COUNT)
        player.executeAndAddTask(5, swordCount) {
            player.world.spawnEntity(EnchantedSwordEntity(player.world, player, target).apply {
                setPosition(player.eyePos - player.rotationVector)
            })
            player.playSoundFromParam(PARAM_SWORD_SOUND, DEFAULT_SWORD_SOUND)
            true
        }
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_GUARD_DURATION, player, DEFAULT_GUARD_DURATION, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val interval = getIntParam(PARAM_SPAWN_INTERVAL, player, DEFAULT_SPAWN_INTERVAL).coerceAtLeast(1)
        if (usedTime % interval == 0) {
            player.attacking?.let { spawnSword(player, it) } ?: player.attacker?.let { spawnSword(player, it) }
        }
    }

    companion object {
        // Default Values
        private const val DEFAULT_GUARD_DURATION = 20 * 20
        private const val DEFAULT_TRIGGER_CHANCE = 0.3f
        private const val DEFAULT_SWORD_COUNT = 1
        private const val DEFAULT_SPAWN_INTERVAL = 25
        private val DEFAULT_SWORD_SOUND = SoundEvents.ENTITY_ARROW_SHOOT

        // Parameter Names
        private const val PARAM_SWORD_SOUND = "sword_sound"  // 剑气音效
        private const val PARAM_GUARD_DURATION = "guard_duration"  // 守护持续时间
        private const val PARAM_TRIGGER_CHANCE = "trigger_chance"  // 触发概率
        private const val PARAM_SWORD_COUNT = "sword_count"  // 剑气数量
        private const val PARAM_SPAWN_INTERVAL = "spawn_interval"  // 生成间隔

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_CHANCE = "chance"  // 对应概率
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
        private const val ENHANCEMENT_INTERVAL = "interval"  // 对应间隔
    }
}