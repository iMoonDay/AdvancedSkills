package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import java.util.function.*

abstract class PassiveSkill(
    id: String,
    types: List<SkillType> = listOf(SkillType.PASSIVE),
    cooldown: Int = 0,
    rarity: Rarity,
    sound: Supplier<SoundEvent>? = null,
    val toggleable: Boolean = false,
) : Skill(id, types, cooldown, rarity, sound),
    EquipTrigger, AttributeTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = if (toggleable)
        UseResult.consume(translateActive(user.toggleUsing(), name.string)) else UseResult.passive(name.string)

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) = player.addAttributes()

    override fun afterRespawn(player: ServerPlayerEntity) =
        player.addAttributes()
}