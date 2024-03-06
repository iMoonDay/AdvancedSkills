package com.imoonday.skill

import com.imoonday.component.toggleUsingSkill
import com.imoonday.trigger.AttributeTrigger
import com.imoonday.trigger.EquipTrigger
import com.imoonday.trigger.RespawnTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent

abstract class PassiveSkill(
    id: String,
    types: Array<SkillType> = arrayOf(SkillType.PASSIVE),
    cooldown: Int = 0,
    rarity: Rarity,
    sound: SoundEvent? = null,
    val toggleable: Boolean = false,
) : Skill(id, types = types, cooldown, rarity, sound),
    EquipTrigger, AttributeTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = if (toggleable) {
        UseResult.consume(
            translateSkill(
                "wall_climbing", if (user.toggleUsingSkill(this)) "active" else "inactive",
                name.string
            )
        )
    } else UseResult.passive(name.string)

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) {
        addAttributes(player)
    }

    override fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        addAttributes(newPlayer)
    }
}