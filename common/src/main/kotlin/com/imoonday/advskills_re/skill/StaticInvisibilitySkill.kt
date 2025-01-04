package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class StaticInvisibilitySkill : PassiveSkill(
    Settings(
        id = "static_invisibility",
        rarity = SkillRarity.EPIC
    )
), AutoTrigger, PersistentTrigger, InvisibilityTrigger {

    override fun shouldStart(player: ServerPlayerEntity): Boolean {
        if (player.hasMoved() || !player.isOnGround && !player.abilities.flying || player.isUsingItem) return false

        val pos = player.pos

        val data = player.getPersistentData()
        if (!data.contains(NBT_LAST_POSITION)) {
            return false
        }

        return NbtUtils.readEntityPositionFromTag(data.getCompound(NBT_LAST_POSITION)) == pos
    }

    override fun shouldStop(player: ServerPlayerEntity): Boolean = !shouldStart(player)

    override fun tick(player: ServerPlayerEntity) {
        super.tick(player)
        if (player.age % 5 == 0) {
            player.getPersistentData().put(NBT_LAST_POSITION, NbtUtils.writeEntityPositionToTag(player.pos))
        }
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        player.clearPersistentData()
    }

    companion object {
        // NBT Keys
        private const val NBT_LAST_POSITION = "LastPosition"  // 上一次位置
    }
}