package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import net.minecraft.entity.player.*

class InsightfulEyeSkill : PassiveSkill(
    id = "insightful_eye",
    rarity = Rarity.EPIC
), FeatureRendererTrigger {

    override fun shouldRenderFeature(target: PlayerEntity, player: PlayerEntity): Boolean =
        player.hasEquipped() && target != player
}