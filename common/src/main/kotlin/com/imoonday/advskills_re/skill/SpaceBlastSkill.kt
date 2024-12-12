package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import net.minecraft.world.*

class SpaceBlastSkill : LongPressSkill(
    id = "space_blast",
    types = listOf(SkillType.DESTRUCTION, SkillType.ATTACK),
    cooldown = 45,
    rarity = SkillRarity.MYTHIC,
    enhancements = setOf(
        SkillEnhancements.RANGE,
        SkillEnhancements.DAMAGE,
        SkillEnhancements.CHARGE_TIME,
        SkillEnhancements.CHARGE_SLOWDOWN,
        SkillEnhancements.DISTANCE
    )
), AttributeTrigger, WorldRendererTrigger, GlowingTrigger {

    override fun getMaxPressTime(): Int = 20 * 5

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Space Blast Charging"),
            "Space Blast Charging",
            player.applyChargeSlowdownEnhancement(-0.4),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        val world = player.world
        forEachBlock(player, {
            val state = world.getBlockState(it)
            !state.isAir && state.getHardness(world, it) >= 0
        }) {
            world.breakBlock(it, false, player)
        }

        val damage = getDamage(player)
        val center = getTargetCenter(player).toCenterPos()
        forEachEntity<LivingEntity>(player) {
            player.sendMessage("${it.displayName.string}: ${center.distanceTo(it.pos)}".toText())
            it.damage(player.damageSources.explosion(player, player), damage)
        }
        player.stopAndCooldown()
        return UseResult.success()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun alwaysKeepCharging(player: PlayerEntity): Boolean = true

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        clientPlayer.isUsing() && entity !== clientPlayer && getEntityStream<LivingEntity>(clientPlayer).contains(entity)

    fun getTargetCenter(player: PlayerEntity): BlockPos =
        player.raycastBlock(
            getMaxDistance(player) * getProgress(player),
            RaycastContext.ShapeType.OUTLINE
        ).pos.toBlockPos()

    fun getAreaRange(player: PlayerEntity): Int =
        4 + player.getEnhancementLvl(SkillEnhancements.RANGE)

    fun getMaxDistance(player: PlayerEntity): Double =
        64.0 * (1.0 + player.getEnhancementLvl(SkillEnhancements.DISTANCE) * 0.2)

    fun getArea(player: PlayerEntity): BlockBox =
        BlockBox(getTargetCenter(player)).expand(getAreaRange(player))

    fun getDamage(player: PlayerEntity): Float =
        getEnhancedValue(player, SkillEnhancements.DAMAGE, 15f)

    fun forEachBlock(player: PlayerEntity, filter: (BlockPos) -> Boolean = { true }, action: (BlockPos) -> Unit) {
        val area = getArea(player)
        val center = getTargetCenter(player)
        val range = getAreaRange(player)
        BlockPos.stream(area).forEach {
            if (it.getSquaredDistance(center) <= range * range && filter(it)) {
                action(BlockPos(it))
            }
        }
    }

    inline fun <reified T : Entity> forEachEntity(
        player: PlayerEntity,
        filter: (T) -> Boolean = { true },
        action: (T) -> Unit
    ) = getEntityStream<T>(player, filter).forEach(action)

    // TODO: 目标中心与实际不符
    inline fun <reified T : Entity> getEntityStream(
        player: PlayerEntity,
        filter: (T) -> Boolean = { true }
    ): List<T> {
        val area = getArea(player)
        val center = getTargetCenter(player)
        val range = getAreaRange(player) + 0.5
        val world = player.world
        val stream = world.getNonSpectatingEntities(T::class.java, Box.from(area))
            .filter { it !== player && it.squaredDistanceTo(center.toCenterPos()) <= range * range && filter(it) }
        return stream
    }
}