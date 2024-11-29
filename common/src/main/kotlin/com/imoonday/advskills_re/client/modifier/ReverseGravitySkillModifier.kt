package com.imoonday.advskills_re.client.modifier

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.util.math.*

class ReverseGravitySkillModifier : IGameRendererModifier<ReverseGravitySkill> {

    override fun modifyWorld(skill: ReverseGravitySkill, tickDelta: Float, limitTime: Long, matrices: MatrixStack) {
        val player = clientPlayer ?: return
        if (!player.isUsing(skill)) return

        matrices.scale(-1f, -1f, 1f)
    }
}