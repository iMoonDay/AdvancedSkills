package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*
import net.minecraft.world.*

class SpaceBlastSkill : LongPressSkill(
    Settings(
        id = "space_blast",
        types = listOf(SkillType.DESTRUCTION, SkillType.ATTACK),
        cooldown = 60,
        rarity = SkillRarity.MYTHIC
    )
), AttributeTrigger, WorldRendererTrigger, GlowingTrigger {

    init {
        settings
            .addParameter(
                name = PARAM_CHARGE_DURATION,
                baseValue = DEFAULT_CHARGE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_BLAST_RANGE,
                baseValue = DEFAULT_BLAST_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_TARGET_DISTANCE,
                baseValue = DEFAULT_TARGET_DISTANCE,
                enhancementId = ENHANCEMENT_DISTANCE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_BLAST_DAMAGE,
                baseValue = DEFAULT_BLAST_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_MOVEMENT_PENALTY,
                baseValue = DEFAULT_MOVEMENT_PENALTY,
                enhancementId = ENHANCEMENT_PENALTY,
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Space Blast Charging"),
            "Space Blast Charging",
            -getDoubleParam(PARAM_MOVEMENT_PENALTY, player, DEFAULT_MOVEMENT_PENALTY),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        val world = player.world
        val center = getTargetCenter(player)
        val damage = getDamage(player)

        player.world.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS)

        val particles: MutableList<ParticleS2CPacket> = mutableListOf()

        forEachEntity<Entity>(player) {
            it.damage(player.damageSources.explosion(player, player), damage)
            particles.add(createParticlePacket(it.centerPos))
        }

        forEachBlock(player, {
            val state = world.getBlockState(it)
            (!state.isAir || !state.fluidState.isEmpty) && state.getHardness(world, it) >= 0
        }) {
            world.breakBlock(it, false, player)
            if (!world.getFluidState(it).isEmpty) {
                world.setBlockState(it, Blocks.AIR.defaultState)
            }
            particles.add(createParticlePacket(it.toCenterPos()))
        }

        player.serverWorld.players.forEach { it.sendPacket(BundleS2CPacket(particles)) }

        player.stopAndCooldown()
        return UseResult.success()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun alwaysKeepCharging(player: PlayerEntity): Boolean = true

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CHARGE_DURATION, player, DEFAULT_CHARGE_DURATION, 0)

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        clientPlayer.isUsing() && entity !== clientPlayer && getEntityStream<Entity>(clientPlayer).contains(entity)

    fun getTargetCenter(player: PlayerEntity): BlockPos =
        player.raycastBlock(
            getMaxDistance(player) * getProgress(player),
            RaycastContext.ShapeType.OUTLINE
        ).pos.toBlockPos()

    fun getAreaRange(player: PlayerEntity): Int =
        getIntParam(PARAM_BLAST_RANGE, player, DEFAULT_BLAST_RANGE)

    fun getMaxDistance(player: PlayerEntity): Double =
        getDoubleParam(PARAM_TARGET_DISTANCE, player, DEFAULT_TARGET_DISTANCE)

    fun getArea(player: PlayerEntity): BlockBox =
        BlockBox(getTargetCenter(player)).expand(getAreaRange(player))

    fun getDamage(player: PlayerEntity): Float =
        getFloatParam(PARAM_BLAST_DAMAGE, player, DEFAULT_BLAST_DAMAGE)

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

    fun createParticlePacket(pos: Vec3d): ParticleS2CPacket = ParticleS2CPacket(
        ParticleTypes.EXPLOSION,
        false, pos.x, pos.y, pos.z,
        0f, 0f, 0f, 0f, 1
    )

    inline fun <reified T : Entity> forEachEntity(
        player: PlayerEntity,
        filter: (T) -> Boolean = { true },
        action: (T) -> Unit
    ) = getEntityStream<T>(player, filter).forEach(action)

    inline fun <reified T : Entity> getEntityStream(
        player: PlayerEntity,
        filter: (T) -> Boolean = { true }
    ): List<T> {
        val area = getArea(player)
        val center = getTargetCenter(player)
        val range = getAreaRange(player) + 0.5
        val world = player.world
        return world.getNonSpectatingEntities(T::class.java, Box.from(area))
            .filter { it !== player && it.squaredDistanceTo(center.toCenterPos()) <= range * range && filter(it) }
    }

    companion object {

        // Default Values
        private const val DEFAULT_CHARGE_DURATION = 5 * 20
        private const val DEFAULT_BLAST_RANGE = 4
        private const val DEFAULT_TARGET_DISTANCE = 64.0
        private const val DEFAULT_BLAST_DAMAGE = 15.0f
        private const val DEFAULT_MOVEMENT_PENALTY = 0.4

        // Parameter Names
        private const val PARAM_CHARGE_DURATION = "charge_duration"  // 蓄力时间
        private const val PARAM_BLAST_RANGE = "blast_range"  // 爆炸范围
        private const val PARAM_TARGET_DISTANCE = "target_distance"  // 目标距离
        private const val PARAM_BLAST_DAMAGE = "blast_damage"  // 爆炸伤害
        private const val PARAM_MOVEMENT_PENALTY = "movement_penalty"  // 移动减速

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "charge_time"  // 对应持续时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围
        private const val ENHANCEMENT_DISTANCE = "distance"  // 对应距离
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应伤害
        private const val ENHANCEMENT_PENALTY = "penalty"  // 对应减速
    }
}