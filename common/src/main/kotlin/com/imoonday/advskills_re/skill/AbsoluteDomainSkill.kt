package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class AbsoluteDomainSkill : Skill(
    Settings.loadOrCreate {
        Settings(
            id = "absolute_domain",
            types = listOf(SkillType.DESTRUCTION),
            cooldown = 15,
            rarity = SkillRarity.RARE
        ).addEnhanceableParameter("persist_time", 3 * 20, "time", 0.2f, Enhancement.Type.MULTIPLY, 5)
            .addEnhanceableParameter("range", 1.0, "range", 1f, Enhancement.Type.ADDITION, 3)
    }
), AutoStopTrigger {

    private val maxHardness = Blocks.OBSIDIAN.hardness

    init {
        addEnhancementDescArg("time") { (it * 100).toInt() }
        addEnhancementDescArg("range") { it }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putDouble("Range", user.getDoubleParameter("range"))
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