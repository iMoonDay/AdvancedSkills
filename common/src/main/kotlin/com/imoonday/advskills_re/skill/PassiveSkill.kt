package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

abstract class PassiveSkill(settings: Settings) : Skill(settings), EquipTrigger, AttributeTrigger, RespawnTrigger {

    override fun initDefaultSettings(settings: Settings) {
        if (isCustomToggles()) {
            settings.addParameter(PARAM_TOGGLEABLE, isToggleable())
        }

        if (!settings.types.contains(SkillType.PASSIVE)) {
            settings.addTypeToTop(SkillType.PASSIVE)
        }
    }

    open fun isToggleable(): Boolean = false

    open fun isCustomToggles(): Boolean = isToggleable()

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

    fun PlayerEntity.isToggleable() = if (!isCustomToggles()) this@PassiveSkill.isToggleable()
    else getBooleanParam(PARAM_TOGGLEABLE, this, this@PassiveSkill.isToggleable())

    fun PlayerEntity.isAvailable() = !isToggleable() || isUsing()

    companion object {

        // Parameter Names
        private const val PARAM_TOGGLEABLE = "toggleable"  // 是否可切换
    }
}