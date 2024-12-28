package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

abstract class PassiveSkill(
    settings: Settings,
    private val toggleable: Boolean = false,
    private val customToggles: Boolean = toggleable
) : Skill(settings), EquipTrigger, AttributeTrigger, RespawnTrigger {

    init {
        if (customToggles) {
            this.settings.addParameter("toggleable", toggleable)
        }

        if (!this.settings.types.contains(SkillType.PASSIVE)) {
            this.settings.addTypeToTop(SkillType.PASSIVE)
        }
    }

    override fun use(user: ServerPlayerEntity): UseResult = if (user.isToggleable()) {
        val active = user.toggleUsing()
        if (active) user.addAttributes() else user.removeAttributes()
        UseResult.consume(translateActive(this, active))
    } else {
        UseResult.passive(name)
    }

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) {
        if (player.isAvailable()) {
            player.addAttributes()
        }
    }

    override fun afterRespawn(player: ServerPlayerEntity) {
        if (player.isAvailable()) {
            player.addAttributes()
        }
    }

    override fun keepUsingAfterRespawn(player: ServerPlayerEntity): Boolean = player.isToggleable()

    fun PlayerEntity.isToggleable() = if (!customToggles) toggleable else getBooleanParam("toggleable", toggleable)

    fun PlayerEntity.isAvailable() = !isToggleable() || isUsing()
}