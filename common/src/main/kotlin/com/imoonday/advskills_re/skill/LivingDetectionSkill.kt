package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class LivingDetectionSkill : Skill(
    id = "living_detection",
    types = listOf(SkillType.UTILITY),
    cooldown = 20,
    rarity = SkillRarity.SUPERB,
), AutoStopTrigger, GlowingTrigger {

    override val persistTime: Int = 5 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        clientPlayer.isUsing() && entity != clientPlayer && entity.isLiving && entity.isAlive && clientPlayer.distanceTo(entity) <= 50 && (entity.x != entity.prevX || entity.y != entity.prevY || entity.z != entity.prevZ)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}