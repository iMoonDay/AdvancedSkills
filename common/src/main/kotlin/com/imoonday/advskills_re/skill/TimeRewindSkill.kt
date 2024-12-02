package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class TimeRewindSkill : LongPressSkill(
    id = "time_rewind",
    types = listOf(SkillType.RESTORATION, SkillType.MOVEMENT),
    cooldown = 60,
    rarity = Rarity.MYTHIC,
), UsingRenderTrigger, DeathTrigger {

    override fun getMaxPressTime(): Int = 5 * 20

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing {
            NbtUtils.writeEntityPositionToTag(player.pos, it)
            it.putFloat("Health", player.health / player.maxHealth)
        }
        return getChargingResult()
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        val data = player.getActiveData()
        NbtUtils.readEntityPositionFromTag(data)?.let {
            player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
            player.requestTeleport(it.x, it.y, it.z)
            player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
        }
        if (data.contains("Health")) {
            player.playSound(ModSounds.HEAL.get())
            player.health = data.getFloat("Health") * player.maxHealth
        }
        player.fallDistance = 0f
        player.stopUsing()
        return UseResult.success()
    }

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean =
        if (player.isUsing()) {
            onRelease(player, player.getUsedTime())
            false
        } else true
}