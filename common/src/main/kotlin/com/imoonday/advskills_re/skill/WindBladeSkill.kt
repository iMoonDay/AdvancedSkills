package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.horizontalRotationVector
import com.imoonday.advskills_re.util.times
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class WindBladeSkill : Skill(
    id = "wind_blade",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = SkillRarity.RARE,
), PostAttackTrigger, PersistentTrigger, DeathTrigger, UsingRenderTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postSweepAttack(player: PlayerEntity, target: LivingEntity) {
        super.postSweepAttack(player, target)
        if (!player.isUsing() || player.world.isClient) return
        player.world.spawnEntity(TornadoEntity(player.world, player, player.horizontalRotationVector * 0.25).apply {
            setPosition(target.pos)
        })
        player.stopAndCooldown()
    }

    override fun onDeath(player: ServerPlayerEntity, source: DamageSource) {
        super.onDeath(player, source)
        if (player.isUsing()) {
            player.startCooling()
        }
    }
}