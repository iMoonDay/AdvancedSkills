package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("summon_sound", SoundEvents.ENTITY_ARROW_SHOOT)
            .addParameter(
                name = timeParamName,
                baseValue = 20 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "spawn_chance",
                baseValue = 0.3f,
                enhancementId = "chance",
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "summon_amount",
                baseValue = 1,
                enhancementId = "amount",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "spawn_interval",
                baseValue = 25,
                enhancementId = "interval",
                value = -2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        val chance = getFloatParam("spawn_chance", player, 0.3f)
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

    private fun spawnSword(
        player: ServerPlayerEntity,
        target: LivingEntity,
    ) {
        if (target.isRemoved || target.isDead) return
        val summonAmount = getIntParam("summon_amount", player, 1)
        player.executeAndAddTask(5, summonAmount) {
            player.world.spawnEntity(EnchantedSwordEntity(player.world, player, target).apply {
                setPosition(player.eyePos - player.rotationVector)
            })
            player.playSoundFromParam("summon_sound", SoundEvents.ENTITY_ARROW_SHOOT)
            true
        }
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val interval = getIntParam("spawn_interval", player, 25).coerceAtLeast(1)
        if (usedTime % interval == 0) {
            player.attacking?.let { spawnSword(player, it) } ?: player.attacker?.let { spawnSword(player, it) }
        }
    }
}