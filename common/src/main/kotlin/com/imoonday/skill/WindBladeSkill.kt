package com.imoonday.skill

import com.imoonday.entity.*
import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.horizontalRotationVector
import com.imoonday.util.times
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class WindBladeSkill : Skill(
    id = "wind_blade",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = Rarity.RARE,
), AttackTrigger, PersistentTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(false)

    override fun postSweepAttack(player: PlayerEntity, target: LivingEntity) {
        super.postSweepAttack(player, target)
        if (!player.isUsing() || player.world.isClient) return
        player.world.spawnEntity(TornadoEntity(player.world, player, player.horizontalRotationVector * 0.25).apply {
            setPosition(target.pos)
        })
        player.stopUsing()
        player.startCooling()
    }

    override fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        if (oldPlayer.isUsing()) newPlayer.startCooling()
    }
}