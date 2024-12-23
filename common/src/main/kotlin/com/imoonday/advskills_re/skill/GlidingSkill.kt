package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

class GlidingSkill : PassiveSkill(
    id = "gliding",
    extraTypes = listOf(SkillType.MOVEMENT),
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), AutoTrigger, SendPlayerDataTrigger {

    override fun shouldStart(player: ServerPlayerEntity): Boolean {
        TODO("Not yet implemented")
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound {
        TODO("Not yet implemented")
    }
}