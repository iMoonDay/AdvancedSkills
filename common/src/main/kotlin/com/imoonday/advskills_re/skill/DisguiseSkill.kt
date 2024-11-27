package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class DisguiseSkill : Skill(
    id = "disguise",
    types = listOf(SkillType.FUNCTION),
    cooldown = 20,
    rarity = Rarity.SUPERB
), DisguiseTrigger, UseInterruptTrigger, AutoStopTrigger {

    override val persistTime: Int = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun interrupt(player: PlayerEntity) {
        player.stopUsing()
        player.startCooling()
    }

    override fun getDisguiseRenderer(
        player: PlayerEntity,
    ): DisguiseTrigger.DisguiseRenderer? {
        var pos = player.blockPos.offset(Direction.DOWN)
        if (player.world.isAir(pos)) {
            pos = pos.offset(Direction.DOWN)
        }
        if (player.world.isAir(pos)) {
            return null
        }
        return DisguiseTrigger.DisguiseRenderer { matrixStack, provider, light, tickDelta ->
            DisguiseTrigger.renderBlockUnderPlayer(
                player,
                pos,
                matrixStack,
                provider,
                light,
                tickDelta
            )
        }
    }
}