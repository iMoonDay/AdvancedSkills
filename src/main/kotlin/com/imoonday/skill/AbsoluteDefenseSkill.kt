package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.FeatureRendererTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents

class AbsoluteDefenseSkill : Skill(
    id = "absolute_defense",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = Rarity.SUPERB
), DamageTrigger, AutoStopTrigger, FeatureRendererTrigger {

    override fun getPersistTime() = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing() || amount <= 0) return amount
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        player.stopUsing()
        return 0.0f
    }

    override fun <T : PlayerEntity, M : EntityModel<T>> render(
        matrices: MatrixStack,
        provider: VertexConsumerProvider,
        light: Int,
        player: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
        renderer: FeatureRendererContext<T, M>,
        context: EntityRendererFactory.Context,
    ) = renderSkillAround(player, tickDelta, matrices, context, provider)
}
