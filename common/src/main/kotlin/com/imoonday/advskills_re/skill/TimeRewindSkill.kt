package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.damage.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class TimeRewindSkill : LongPressSkill(
    id = "time_rewind",
    types = listOf(SkillType.RESTORATION, SkillType.MOVEMENT),
    cooldown = 60,
    rarity = SkillRarity.MYTHIC,
), UsingRenderTrigger, DeathTrigger {

    override fun getMaxPressTime(): Int = 5 * 20

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing {
            NbtUtils.writeEntityPositionToTag(player.pos, it)
            it.putString("Dimension", player.world.registryKey.value.toString())
            it.putFloat("Health", player.health / player.maxHealth)
        }
        return getChargingResult()
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        val data = player.getActiveData()
        NbtUtils.readEntityPositionFromTag(data)?.let {
            data.getString("Dimension").toIdentifier()?.let { id ->
                player.server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id))?.let { world ->
                    player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
                    player.teleport(world, it.x, it.y, it.z, emptySet(), player.yaw, player.pitch)
                    player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
                }
            }
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