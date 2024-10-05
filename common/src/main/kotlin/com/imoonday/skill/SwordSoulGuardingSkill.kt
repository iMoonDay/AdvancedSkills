package com.imoonday.skill

import com.imoonday.entity.EnchantedSwordEntity
import com.imoonday.entity.render.EnchantedSwordEntityRenderer
import com.imoonday.trigger.AttackTrigger
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.FeatureRendererTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.minus
import com.imoonday.util.playSound
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.RotationAxis

class SwordSoulGuardingSkill : Skill(
    id = "sword_soul_guarding",
    types = listOf(SkillType.SUMMON, SkillType.ATTACK),
    cooldown = 30,
    rarity = Rarity.LEGENDARY,
), AttackTrigger, DamageTrigger, AutoStopTrigger, FeatureRendererTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float {
        if (source.source !is EnchantedSwordEntity && player.random.nextFloat() < 0.3) spawnSword(player, target)
        return super.onAttack(amount, source, player, target)
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        attacker?.let {
            spawnSword(player, attacker)
        }
        return super.onDamaged(amount, source, player, attacker)
    }

    private fun spawnSword(
        player: ServerPlayerEntity,
        target: LivingEntity,
    ) {
        if (target.isRemoved) return
        player.world.spawnEntity(EnchantedSwordEntity(player.world, player, target).apply {
            setPosition(player.eyePos - player.rotationVector)
        })
        player.playSound(SoundEvents.ENTITY_ARROW_SHOOT)
    }

    override fun getPersistTime(): Int = 20 * 10

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && usedTime % 25 == 0)
            if (player.attacking != null) {
                spawnSword(player, player.attacking!!)
            } else if (player.attacker != null)
                spawnSword(player, player.attacker!!)
        super.serverTick(player, usedTime)
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
    ) {
        matrices.push()
        matrices.translate(0f, 0f, 0.5f)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - headYaw))
        matrices.multiply(
            RotationAxis.NEGATIVE_X.rotationDegrees(180f)
        )
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45f))
        context.itemRenderer.renderItem(
            EnchantedSwordEntityRenderer.sword,
            ModelTransformationMode.GROUND,
            context.renderDispatcher.getLight(player, tickDelta),
            OverlayTexture.DEFAULT_UV,
            matrices,
            provider,
            player.world,
            0
        )
        matrices.pop()
    }
}