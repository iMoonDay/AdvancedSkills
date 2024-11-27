package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.damage.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import kotlin.math.*

class TimeRewindSkill : LongPressSkill(
    id = "time_rewind",
    types = listOf(SkillType.RESTORATION, SkillType.MOVEMENT),
    cooldown = 60,
    rarity = Rarity.MYTHIC,
), UsingRenderTrigger, DeathTrigger {

    override fun getMaxPressTime(): Int = 20 * 5

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.properties.getCompound("backups").run {
            keys.mapNotNull { it.toIntOrNull() }
                .minByOrNull { (player.age - pressedTime - it).absoluteValue }
                ?.let { age ->
                    getCompound(age.toString()).run {
                        NbtUtils.readEntityPositionFromTag(this)?.let {
                            player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
                            player.requestTeleport(it.x, it.y, it.z)
                            player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
                        }
                        if (contains("Health")) {
                            player.playSound(ModSounds.HEAL.get())
                            player.health = getFloat("Health") * player.maxHealth
                        }
                        player.fallDistance = 0f
                        player.properties.remove("backups")
                        player.stopUsing()
                    }
                }
        }
        return UseResult.success()
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isReady()) {
            player.properties.put(
                "backups",
                player.properties.getCompound("backups").apply {
                    put(
                        player.age.toString(),
                        NbtUtils.writeEntityPositionToTag(
                            player.pos,
                            NbtCompound().apply { putFloat("Health", player.health / player.maxHealth) })
                    )
                    keys.filter { (it.toIntOrNull() ?: 0) < player.age - 20 * 5 }.forEach { remove(it) }
                })
        }
        super.serverTick(player, usedTime)
    }

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean =
        if (player.isUsing()) {
            onRelease(player, player.getUsedTime())
            false
        } else true
}