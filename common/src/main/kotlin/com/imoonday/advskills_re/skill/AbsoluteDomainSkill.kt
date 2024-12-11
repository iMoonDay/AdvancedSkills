package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.text.*

class AbsoluteDomainSkill : Skill(
    id = "absolute_domain",
    types = listOf(SkillType.DESTRUCTION),
    cooldown = 15,
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.RANGE)
), AutoStopTrigger {

    private val maxHardness = Blocks.OBSIDIAN.hardness

    override val persistTime: Int = 3 * 20

    init {
        addEnhancementTooltipWithArg(SkillEnhancements.RANGE) { it.level * 0.5 }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putDouble("Range", user.getEnhancementLvl(SkillEnhancements.RANGE) * 0.5)
    })

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val range = player.getActiveData().getDouble("Range")
        player.boundingBox.expand(1.0 + range).blockPosSet.filter {
            val hardness = player.world.getBlockState(it).getHardness(player.world, it)
            hardness < maxHardness && hardness >= 0 && it.y >= player.blockY
        }.forEach {
            val centerPos = it.toCenterPos()
            player.spawnParticles(ParticleTypes.SMOKE, false, centerPos, 1, 0.0, 0.0, 0.0, 0.0)
            player.world.breakBlock(it, true, player)
        }
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}