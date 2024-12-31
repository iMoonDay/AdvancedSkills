package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.fluid.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class LavaWalkerSkill : Skill(
    id = "lava_walker",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), WalkOnFluidTrigger,
    AutoStopTrigger,
    FluidMovementTrigger,
    UsingRenderTrigger,
    DamageTrigger,
    LavaTrigger {

    init {
        addParameter(
            name = timeParamName,
            baseValue = 20 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this) {
        user.playSound(SoundEvents.BLOCK_LAVA_AMBIENT)
    }

    override fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean =
        player.isUsing() && state.isOf(Fluids.LAVA) && player.getFluidHeight(FluidTags.LAVA) < 0.02

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun ignoreFluid(player: PlayerEntity, tag: TagKey<Fluid>): Boolean {
        val isOnLava = player.world.getFluidState(player.blockPos)
            .isIn(FluidTags.LAVA) && player.world.getFluidState(player.eyePos.toBlockPos()).isEmpty
        val fluidHeight = player.world.getFluidState(player.blockPos).height - (player.y - player.blockY)
        return player.isUsing() && tag == FluidTags.LAVA && (isOnLava && fluidHeight < 0.02)
    }

    override fun getMovementInFluid(player: PlayerEntity, tag: TagKey<Fluid>, speed: Double): Double =
        if (!player.isUsing() || tag != FluidTags.LAVA) speed else 0.0

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?
    ): Boolean =
        player.isUsing() && source.isOf(DamageTypes.HOT_FLOOR) || super.ignoreDamage(amount, source, player, attacker)

    override fun ignoreLava(player: PlayerEntity): Boolean = ignoreFluid(player, FluidTags.LAVA)
}