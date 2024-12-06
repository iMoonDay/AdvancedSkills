package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import net.minecraft.entity.player.*

class InsightfulEyeSkill : PassiveSkill(
    id = "insightful_eye",
    rarity = SkillRarity.EPIC
), FeatureRendererTrigger {

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean =
        clientPlayer.hasEquipped() && target != clientPlayer
}