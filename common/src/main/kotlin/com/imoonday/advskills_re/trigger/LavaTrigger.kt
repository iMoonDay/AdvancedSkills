package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface LavaTrigger : SkillTrigger {

    fun ignoreLava(player: PlayerEntity): Boolean
}