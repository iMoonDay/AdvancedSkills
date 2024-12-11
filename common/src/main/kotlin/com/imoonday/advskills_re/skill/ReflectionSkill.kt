package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

abstract class ReflectionSkill(
    id: String,
    types: List<SkillType> = listOf(SkillType.DEFENSE),
    cooldown: Int,
    rarity: SkillRarity,
    duration: Int,
    enhancements: Set<SkillEnhancementType<*>> = setOf(SkillEnhancements.PERSISTENT_TIME)
) : Skill(
    id = id,
    types = types,
    cooldown = cooldown,
    rarity = rarity,
    enhancements = enhancements
), DamageTrigger, ReflectionTrigger, UsingRenderTrigger {

    override val persistTime: Int = duration

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    protected fun reflectedFailed(player: ServerPlayerEntity) =
        player.sendMessage(translate("reflection.failed"), true)

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        attacker?.damage(player.damageSources.thorns(player), amount)?.let {
            player.sendMessage(
                translate("reflection.${if (it) "success" else "failed"}"),
                true
            )
        }
    }

    override fun getRenderModel(target: PlayerEntity, clientPlayer: PlayerEntity): ModelIdentifier =
        Skills.ABSOLUTE_DEFENSE.modelId
}