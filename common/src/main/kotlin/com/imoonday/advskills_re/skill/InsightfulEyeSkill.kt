package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import net.minecraft.entity.player.*

class InsightfulEyeSkill : PassiveSkill(
    Settings(
        id = "insightful_eye",
        rarity = SkillRarity.EPIC
    )
), FeatureRendererTrigger {

    override fun isCustomToggles(): Boolean = true

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean =
        clientPlayer.hasEquipped() && clientPlayer.isAvailable() && target != clientPlayer
}