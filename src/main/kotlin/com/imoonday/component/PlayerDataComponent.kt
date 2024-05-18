package com.imoonday.component

import com.imoonday.screen.SkillLearningScreen
import com.imoonday.trigger.ClientUseTrigger
import com.imoonday.util.*
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

interface DataComponent : Component {

    var container: SkillContainer
    var level: SkillLevelData
    var learnable: LearnableSkillData

    fun reset()
}

class PlayerDataComponent(private val player: PlayerEntity) :
    DataComponent,
    AutoSyncedComponent,
    CommonTickingComponent {

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
        (player as? ServerPlayerEntity)?.run {
            if (learnable.correct(learnedSkills)) syncData()
        }
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        val oldSkills = container.getAllSkills { _, data -> data.using }
        val hasChoice = learnable.hasNext()
        super.applySyncPacket(buf)
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

    override fun reset() {
        if (!player.world.isClient) {
            container = SkillContainer()
            level = SkillLevelData()
            learnable = LearnableSkillData()
            player.syncData()
        }
    }
}