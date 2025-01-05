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

    override fun use(user: ServerPlayerEntity): UseResult = if (isToggleable(user)) {
        val active = user.toggleUsing()
        if (active) onActivated(user) else onDeactivated(user)
        UseResult.consume(translateActive(this, active))
    } else {
        UseResult.passive(name)
    }

    open fun onActivated(user: ServerPlayerEntity) {
        user.addAttributes()
    }

    open fun onDeactivated(user: ServerPlayerEntity) {
        user.removeAttributes()
    }

    override fun postEquipped(player: ServerPlayerEntity, slot: SkillSlot) {
        if (isAvailable(player)) {
            onActivated(player)
        }
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        onDeactivated(player)
    }

    override fun afterRespawn(player: ServerPlayerEntity) {
        if (isAvailable(player)) {
            onActivated(player)
        }
    }

    override fun keepUsingAfterRespawn(player: ServerPlayerEntity): Boolean = isToggleable(player)

    fun isToggleable(player: PlayerEntity) = if (!isCustomToggles()) this.isToggleable()
    else getBooleanParam(PARAM_TOGGLEABLE, player, isToggleable())

    fun isAvailable(player: PlayerEntity) = !isToggleable(player) || player.isUsing()

    companion object {

        // Parameter Names
        private const val PARAM_TOGGLEABLE = "toggleable"  // 是否可切换
    }
}