package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.screen.*
import com.imoonday.advskills_re.config.*
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
    var learnable: LearnableSkillData = LearnableSkillData()
    var enhancements: LearnableEnhancementData = LearnableEnhancementData()

    override fun readFromNbt(tag: NbtCompound) {
        if (tag.contains("container")) {
            container = SkillContainer.fromNbt(tag.getCompound("container"))
        }
        if (tag.contains("level")) {
            level = SkillLevelData.fromNbt(tag.getCompound("level"))
        }
        if (tag.contains("learnable")) {
            learnable = LearnableSkillData.fromNbt(tag.getCompound("learnable"))
        }
        if (tag.contains("enhancements")) {
            enhancements = LearnableEnhancementData.fromNbt(tag.getCompound("enhancements"))
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("container", container.toNbt())
        tag.put("level", level.toNbt())
        tag.put("learnable", learnable.toNbt())
        tag.put("enhancements", enhancements.toNbt())
    }

    override fun tick() {
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
            if (entity.hasLearnedAll()) {
                if (learnable.count > 0) {
                    enhancements.count += learnable.count * 3
                    learnable.reset()
                    dirty = true
                }
            } else if (enhancements.count > 0) {
                learnable.count += enhancements.count / 3
                enhancements.reset()
                dirty = true
            }

            val result1 = learnable.correct(entity.learnedSkills, GlobalConfig.get().getLearningFilter())
            val result2 = enhancements.correct(entity)
            if (dirty || result1 || result2) {
                sync()
                dirty = false
            }
        }
    }

    override fun clientTick() {
        super.clientTick()
        container.getAllSkills { skill, _ -> !entity.hasLearned(skill) }.forEach { container.forget(it) }
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
        val hasSkillChoice = learnable.hasNext()
        val hasEnhancementChoice = enhancements.hasNext()
        super.applySyncNbt(tag)
        val newSkills = container.getAllSkills { _, data -> data.using }
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse(entity) }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop(entity) }
        if (entity.isCurrentClientPlayer) {
            if (!hasSkillChoice && learnable.hasNext()) {
                SkillLearningScreen.new = true
            } else if (!hasEnhancementChoice && enhancements.hasNext()) {
                SkillEnhancementScreen.new = true
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
        learnable = LearnableSkillData()
        enhancements = LearnableEnhancementData()
        synced = false
        sync()
    }
}