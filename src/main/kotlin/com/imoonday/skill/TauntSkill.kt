package com.imoonday.skill

import com.imoonday.entity.Servant
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.FeatureRendererTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class TauntSkill : Skill(
    id = "taunt",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.SUPERB,
), DamageTrigger, AutoStopTrigger, FeatureRendererTrigger {

    override fun getPersistTime(): Int = 20 * 15

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing() || attacker !is Servant) amount else amount * 0.75f

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
    ) = renderSkillAboveHead(matrices, context, provider, player)

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = !player.isUsing()
}