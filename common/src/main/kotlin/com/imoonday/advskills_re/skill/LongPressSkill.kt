package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

abstract class LongPressSkill(settings: Settings) : Skill(settings), LongPressTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = onRelease(user, 1)

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) = Unit

    override fun shouldFlashIcon(player: PlayerEntity): Boolean = false
}