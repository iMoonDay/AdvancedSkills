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

    override val timeParamName: String = AutoStopTrigger.CHARGE_TIME

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = AutoStopTrigger.CHARGE_TIME,
                baseValue = 5 * 20,
                enhancementId = "time",
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "area_range",
                baseValue = 4,
                enhancementId = "range",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "max_distance",
                baseValue = 64.0,
                enhancementId = "distance",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage",
                baseValue = 15.0f,
                enhancementId = "damage",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "charge_slowdown",
                baseValue = 0.4,
                enhancementId = "slowdown",
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
            -getDoubleParam("charge_slowdown", player, 0.4),
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

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean =
        clientPlayer.isUsing() && entity !== clientPlayer && getEntityStream<Entity>(clientPlayer).contains(entity)

    fun getTargetCenter(player: PlayerEntity): BlockPos =
        player.raycastBlock(
            getMaxDistance(player) * getProgress(player),
            RaycastContext.ShapeType.OUTLINE
        ).pos.toBlockPos()

    fun getAreaRange(player: PlayerEntity): Int =
        getIntParam("area_range", player, 4)

    fun getMaxDistance(player: PlayerEntity): Double =
        getDoubleParam("max_distance", player, 64.0)

    fun getArea(player: PlayerEntity): BlockBox =
        BlockBox(getTargetCenter(player)).expand(getAreaRange(player))

    fun getDamage(player: PlayerEntity): Float =
        getFloatParam("damage", player, 15.0f)

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
}