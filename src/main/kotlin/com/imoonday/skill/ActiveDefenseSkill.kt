package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.init.ModSkills
import com.imoonday.trigger.AttributeTrigger
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
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class ActiveDefenseSkill : LongPressSkill(
    id = "active_defense",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 10,
    rarity = Rarity.SUPERB,
), DamageTrigger, AttributeTrigger, FeatureRendererTrigger {

    override fun getMaxPressTime(): Int = 10 * 10
    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Active Defense"),
            "Active Defense",
            -0.5,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        player.startCooling()
        if (pressedTime.toFloat() / getMaxPressTime() < 0.5f) {
            player.modifyCooldown { it / 2 }
        }
        player.removeAttributes()
        return UseResult.consume()
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount else amount * 0.8f

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

    override fun shouldRender(player: PlayerEntity): Boolean =
        player.isUsing() && !player.isUsingSkill(ModSkills.ABSOLUTE_DEFENSE)
}