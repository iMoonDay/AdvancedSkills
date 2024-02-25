package com.imoonday.skills

import com.imoonday.trigger.AttributeTrigger
import com.imoonday.trigger.EquipTrigger
import com.imoonday.trigger.RespawnTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillSlot
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent

abstract class PassiveSkill(
    id: String,
    types: Array<SkillType> = arrayOf(SkillType.PASSIVE),
    cooldown: Int = 0,
    rarity: Rarity,
    sound: SoundEvent? = null,
) : Skill(id, types = types, cooldown, rarity, sound),
    EquipTrigger, AttributeTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) {
        addAttributes(player)
    }

    override fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        addAttributes(newPlayer)
    }
}