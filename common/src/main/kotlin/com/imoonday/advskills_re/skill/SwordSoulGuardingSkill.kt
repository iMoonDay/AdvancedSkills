package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.entity.render.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.minus
import com.imoonday.advskills_re.util.playSound
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.render.model.json.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*

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

    override val persistTime: Int = 20 * 10

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