package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.trigger.renderer.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class SwordSoulGuardingSkill : Skill(
    id = "sword_soul_guarding",
    types = listOf(SkillType.SUMMON, SkillType.ATTACK),
    cooldown = 30,
    rarity = Rarity.LEGENDARY,
), PostAttackTrigger, PostAttackedTrigger, AutoStopTrigger, UsingRenderTrigger, DangerTrigger {

    override val persistTime: Int = 20 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (player.isUsing() && source.source !is EnchantedSwordEntity && player.random.nextFloat() < 0.3) {
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
        if (target.isRemoved) return
        player.world.spawnEntity(EnchantedSwordEntity(player.world, player, target).apply {
            setPosition(player.eyePos - player.rotationVector)
        })
        player.playSound(SoundEvents.ENTITY_ARROW_SHOOT)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && usedTime % 25 == 0) {
            player.attacking?.let { spawnSword(player, it) } ?: player.attacker?.let { spawnSword(player, it) }
        }
        super.serverTick(player, usedTime)
    }
}