package com.imoonday.skill

import com.imoonday.component.properties
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import fi.dy.masa.malilib.util.NBTUtils
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import kotlin.math.absoluteValue

class TimeRewindSkill : LongPressSkill(
    id = "time_rewind",
    types = listOf(SkillType.RESTORATION, SkillType.MOVEMENT),
    cooldown = 60,
    rarity = Rarity.MYTHIC,
), UsingRenderTrigger {

    override fun getMaxPressTime(): Int = 20 * 5

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.properties.getCompound("backups").run {
            keys.mapNotNull { it.toIntOrNull() }
                .minByOrNull { (player.age - pressedTime - it).absoluteValue }
                ?.let { age ->
                    getCompound(age.toString()).run {
                        NBTUtils.readEntityPositionFromTag(this)?.let {
                            player.playSound(SoundEvents.ENTITY_FOX_TELEPORT)
                            player.requestTeleport(it.x, it.y, it.z)
                            player.fallDistance = 0f
                            player.properties.remove("backups")
                            player.stopUsing()
                        }
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
                    put(player.age.toString(), NBTUtils.writeEntityPositionToTag(player.pos, NbtCompound()))
                    keys.filter { (it.toIntOrNull() ?: 0) < player.age - 20 * 5 }.forEach { remove(it) }
                })
        }
        super.serverTick(player, usedTime)
    }
}