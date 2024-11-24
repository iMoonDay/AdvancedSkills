package com.imoonday.component

import com.imoonday.client.screen.*
import com.imoonday.network.*
import com.imoonday.network.s2c.*
import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*

interface DataComponent : Component {

    var container: SkillContainer
    var level: SkillLevelData
    var learnable: LearnableSkillData

    fun reset()
}

class PlayerDataComponent(private val player: PlayerEntity) : DataComponent {

    var dirty: Boolean = false
    override var container: SkillContainer = SkillContainer()
    override var level: SkillLevelData = SkillLevelData()
    override var learnable: LearnableSkillData = LearnableSkillData()

    override fun readFromNbt(tag: NbtCompound) {
        container = SkillContainer.fromNbt(tag.getCompound("container"))
        level = SkillLevelData.fromNbt(tag.getCompound("level"))
        learnable = LearnableSkillData.fromNbt(tag.getCompound("learnable"))
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("container", container.toNbt())
        tag.put("level", level.toNbt())
        tag.put("learnable", learnable.toNbt())
    }

    override fun tick() {
        container.forEachData { it.tick() }
        container.getAllSlots { it.skill.invalid && !it.isEmpty() }.forEach {
            val name = it.skill.name.string
            it.unequip()
            if (!player.world.isClient) {
                player.sendMessage(translate("unequipSkill", "banned", name))
            }
            player.updateScreen()
        }
        if (player is ServerPlayerEntity && (dirty || learnable.correct(player.learnedSkills))) {
            sync()
            dirty = false
        }
    }

    override fun applySyncNbt(tag: NbtCompound) {
        val oldSkills = container.getAllSkills { _, data -> data.using }
        val hasChoice = learnable.hasNext()
        super.applySyncNbt(tag)
        val newSkills = container.getAllSkills { _, data -> data.using }
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse(player) }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop(player) }
        if (!hasChoice && learnable.hasNext()) {
            SkillLearningScreen.new = true
        }
        player.updateScreen()
    }

    override fun sync() {
        (player.world as? ServerWorld)?.let {
            Channels.SYNC_PLAYER_DATA_S2C.sendToPlayers(
                it.players,
                SyncPlayerDataS2CPacket(player.id, toNbt())
            )
        }
    }

    override fun reset() {
        if (!player.world.isClient) {
            container = SkillContainer()
            level = SkillLevelData()
            learnable = LearnableSkillData()
            player.syncData()
        }
    }
}