package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*

class PlayerDataComponent(override val entity: PlayerEntity) : Component<PlayerEntity> {

    var dirty: Boolean = false
    override var synced: Boolean = false
    var container: SkillContainer = SkillContainer()
    var level: SkillLevelData = SkillLevelData()
    var choiceData: ChoiceData = ChoiceData()

    override fun readFromNbt(tag: NbtCompound) {
        if (tag.contains("container")) {
            container = SkillContainer.fromNbt(tag.getCompound("container"))
        }
        if (tag.contains("level")) {
            level = SkillLevelData.fromNbt(tag.getCompound("level"))
        }
        if (tag.contains("choiceData")) {
            choiceData = ChoiceData.fromNbt(tag.getCompound("choiceData"))
        } else if (tag.contains("learnable")) {
            choiceData = ChoiceData.fromNbt(tag.getCompound("learnable"))
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("container", container.toNbt())
        tag.put("level", level.toNbt())
        tag.put("choiceData", choiceData.toNbt())
    }

    override fun tick() {
        container.getAllSkills { skill, _ -> !entity.hasLearned(skill) }.forEach { container.forget(it) }
        container.forEachData { it.tick() }
        container.getAllSlots { it.skill.invalid && !it.isEmpty() }.forEach {
            val name = it.skill.name
            it.unequip()
            if (!entity.world.isClient) {
                entity.sendMessage(translate("unequipSkill.banned", name))
            } else {
                entity.updateScreen()
            }
        }
        if (entity is ServerPlayerEntity) {
            val result = choiceData.correct(entity)
            if (dirty || result) {
                sync()
                dirty = false
            }
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
        val oldSkills = container.getAllSkills { _, data -> data.using }
        val wasEmpty = choiceData.isEmpty()
        super.applySyncNbt(tag)
        val newSkills = container.getAllSkills { _, data -> data.using }
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse(entity) }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop(entity) }
        if (entity.isCurrentClientPlayer) {
            if (wasEmpty && !choiceData.isEmpty()) {
                SkillChoiceScreen.new = true
            }
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
        container = SkillContainer()
        level = SkillLevelData()
        choiceData = ChoiceData()
        synced = false
        sync()
    }
}