package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import java.util.function.*

abstract class PassiveSkill(
    id: String,
    extraTypes: List<SkillType> = emptyList(),
    cooldown: Int = 0,
    rarity: SkillRarity,
    sound: Supplier<SoundEvent>? = null,
    val toggleable: Boolean = false,
    enhancements: Set<SkillEnhancementType<*>> = emptySet(),
) : Skill(id, (setOf(SkillType.PASSIVE) + extraTypes).toList(), cooldown, rarity, sound, enhancements),
    EquipTrigger, AttributeTrigger, RespawnTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = if (toggleable)
        UseResult.consume(translateActive(this, user.toggleUsing())) else UseResult.passive(name)

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) = player.addAttributes()

    override fun afterRespawn(player: ServerPlayerEntity) =
        player.addAttributes()
}