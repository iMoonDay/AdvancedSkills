package com.imoonday.component

import com.imoonday.trigger.ClientUseTrigger
import com.imoonday.util.SkillContainer
import com.imoonday.util.SkillLevelData
import com.imoonday.util.translate
import com.imoonday.util.updateScreen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

interface DataComponent : Component {

    var container: SkillContainer
    var level: SkillLevelData
}

class PlayerDataComponent(private val player: PlayerEntity) :
    DataComponent,
    AutoSyncedComponent,
    CommonTickingComponent {

    override var container: SkillContainer = SkillContainer()
    override var level: SkillLevelData = SkillLevelData()

    override fun readFromNbt(tag: NbtCompound) {
        container = SkillContainer.fromNbt(tag.getCompound("container"))
        level = SkillLevelData.fromNbt(tag.getCompound("level"))
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("container", container.toNbt())
        tag.put("level", level.toNbt())
    }

    override fun tick() {
        container.forEachData { it.tick() }
        container.getAllSlots { it.skill.invalid && !it.isEmpty() }.forEach {
            val name = it.skill.name.string
            it.unequip()
            if (!player.world.isClient) {
                player.sendMessage(translate("unequipSkill", "banned", name))
            }
        }
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        val oldSkills = container.getAllSkills { _, data -> data.using }
        super.applySyncPacket(buf)
        val newSkills = container.getAllSkills { _, data -> data.using }
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse() }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop() }
        player.updateScreen()
    }
}