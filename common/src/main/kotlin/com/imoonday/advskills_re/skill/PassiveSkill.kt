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
        settings.addTypeToTopIfAbsent(SkillType.PASSIVE)

        if (customToggles) {
            settings.addParameter(PARAM_TOGGLEABLE, toggleable)
        }
    }

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

    fun isToggleable(player: PlayerEntity) = if (!customToggles) toggleable
    else getBooleanParam(PARAM_TOGGLEABLE, player, toggleable)

    fun isAvailable(player: PlayerEntity) = !isToggleable(player) || player.isUsing()

    companion object {

        // Parameter Names
        private const val PARAM_TOGGLEABLE = "toggleable"  // 是否可切换
    }
}