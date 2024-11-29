package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*

class PlayerDataComponent(override val entity: PlayerEntity) : Component<PlayerEntity> {

    var dirty: Boolean = false
    override var synced: Boolean = false
    var container: SkillContainer = SkillContainer.create(entity.world)
    var level: SkillLevelData = SkillLevelData()
    var learnable: LearnableSkillData = LearnableSkillData()

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
        container.getAllSlots { it.skill.isInvalid(entity.world) && !it.isEmpty() }.forEach {
            val name = it.skill.name
            it.unequip()
            if (!entity.world.isClient) {
                entity.sendMessage(translate("unequipSkill.banned", name))
            } else {
                entity.updateScreen()
            }
        }
        if (entity is ServerPlayerEntity && (dirty || learnable.correct(entity.world, entity.learnedSkills))) {
            sync()
            dirty = false
        }
    }

    override fun requestSync() {
        Channels.REQUEST_SYNC_COMPONENT_C2S.sendToServer(
            RequestSyncComponentC2SRequest(
                entity.id,
                RequestSyncComponentC2SRequest.ComponentType.PLAYER_DATA,
                RequestSyncComponentC2SRequest.Receiver.SENDER
            )
        )
    }

    override fun applySyncNbt(tag: NbtCompound) {
        val oldSkills = container.getAllSkills(entity.world) { _, data -> data.using }
        val hasChoice = learnable.hasNext()
        super.applySyncNbt(tag)
        val newSkills = container.getAllSkills(entity.world) { _, data -> data.using }
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse(entity) }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop(entity) }
        if (!hasChoice && learnable.hasNext()) {
            SkillLearningScreen.new = true
        }
        if (entity.world.isClient) {
            entity.updateScreen()
        }
    }

    override fun sync() {
        (entity.world as? ServerWorld)?.let {
            RequestSyncComponentC2SRequest.Receiver.NEARBY_PLAYERS.send(
                Channels.SYNC_PLAYER_DATA_S2C,
                SyncPlayerDataS2CPacket(entity.id, toNbt()),
                null,
                entity
            )
        }
    }

    fun reset() {
        container = SkillContainer.create(entity.world)
        level = SkillLevelData()
        learnable = LearnableSkillData()
        sync()
    }
}