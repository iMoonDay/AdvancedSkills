package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class SwordSoulGuardingSkill : Skill(
    id = "sword_soul_guarding",
    types = listOf(SkillType.SUMMON, SkillType.ATTACK),
    cooldown = 30,
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(
        SkillEnhancements.PERSISTENT_TIME,
        SkillEnhancements.CHANCE,
        SkillEnhancements.SUMMON_AMOUNT,
        SkillEnhancements.EFFECT_FREQUENCY
    )
), PostAttackTrigger, PostAttackedTrigger, AutoStopTrigger, UsingRenderTrigger, DangerTrigger {

    init {
        addEnhanceableParameter(timeParameterName, 20 * 20, "time", 0.2f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        val extraChance = player.getEnhancementLvl(SkillEnhancements.CHANCE) * 0.1
        if (player.isUsing() && source.source !is EnchantedSwordEntity && player.random.nextFloat() < 0.3 + extraChance) {
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

    private fun spawnSword(
        player: ServerPlayerEntity,
        target: LivingEntity,
    ) {
        if (target.isRemoved || target.isDead) return
        val summonAmount = player.getEnhancementLvl(SkillEnhancements.SUMMON_AMOUNT)
        player.executeAndAddTask(5, summonAmount) {
            player.world.spawnEntity(EnchantedSwordEntity(player.world, player, target).apply {
                setPosition(player.eyePos - player.rotationVector)
            })
            player.playSound(SoundEvents.ENTITY_ARROW_SHOOT)
            true
        }
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val frequency = (25 - player.getEnhancementLvl(SkillEnhancements.EFFECT_FREQUENCY) * 2).coerceAtLeast(1)
        if (usedTime % frequency == 0) {
            player.attacking?.let { spawnSword(player, it) } ?: player.attacker?.let { spawnSword(player, it) }
        }
    }
}