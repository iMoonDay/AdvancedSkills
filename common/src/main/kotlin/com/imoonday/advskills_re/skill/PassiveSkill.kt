package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import java.util.function.*

abstract class PassiveSkill(
    id: String,
    extraTypes: List<SkillType> = emptyList(),
    cooldown: Int = 0,
    rarity: Rarity,
    sound: Supplier<SoundEvent>? = null,
    val toggleable: Boolean = false,
) : Skill(id, (setOf(SkillType.PASSIVE) + extraTypes).toList(), cooldown, rarity, sound),
    EquipTrigger, AttributeTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = if (toggleable)
        UseResult.consume(translateActive(user.toggleUsing(), name)) else UseResult.passive(name)

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) = player.addAttributes()

    override fun afterRespawn(player: ServerPlayerEntity) =
        player.addAttributes()
}