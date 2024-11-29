package com.imoonday.advskills_re.client.render.renderer

import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.*

open class CrosshairRenderer<T> : ICrosshairRenderer<T> where T : Skill, T : CrosshairTrigger {

    override fun render(skill: T, context: DrawContext) {
        val player = clientPlayer ?: return

        skill.getCrosshair(player).draw(context)
    }

    override fun shouldRenderCrosshair(skill: T): Boolean {
        val player = clientPlayer ?: return false
        return skill.shouldRenderCrosshair(player)
    }

    override fun getPriority(skill: T): Int {
        val player = clientPlayer ?: return 0
        return skill.getPriority(player)
    }
}