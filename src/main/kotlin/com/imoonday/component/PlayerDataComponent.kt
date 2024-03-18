package com.imoonday.component

import com.imoonday.init.ModSkills
import com.imoonday.skill.Skill
import com.imoonday.trigger.ClientUseTrigger
import com.imoonday.util.SkillData
import com.imoonday.util.SkillLevelData
import com.imoonday.util.SkillSlot
import com.imoonday.util.updateScreen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

interface DataComponent : Component {

    val learned: MutableMap<Skill, SkillData>
    val equipped: MutableMap<SkillSlot, Skill>
    val level: SkillLevelData
}

class PlayerDataComponent(private val player: PlayerEntity) :
    DataComponent,
    AutoSyncedComponent,
    CommonTickingComponent {

    override val learned: MutableMap<Skill, SkillData> = mutableMapOf()
    override val equipped: MutableMap<SkillSlot, Skill> =
        SkillSlot.entries.filter { it.valid }
            .associateWith { ModSkills.EMPTY }
            .toMutableMap()
    override val level: SkillLevelData = SkillLevelData()

    override fun readFromNbt(tag: NbtCompound) {
        learned.clear()
        equipped.clear()
        tag.getList("learned", NbtElement.COMPOUND_TYPE.toInt())
            .filterIsInstance<NbtCompound>()
            .forEach {
                val skill = Skill.fromIdNullable(Identifier(it.getString("id")))
                val data = SkillData.fromNbt(it.getCompound("data"))
                if (skill != null) learned[skill] = data
            }
        tag.getList("equipped", NbtElement.COMPOUND_TYPE.toInt())
            .filterIsInstance<NbtCompound>()
            .forEach {
                val skill = Skill.fromId(Identifier(it.getString("id")))
                val slot = SkillSlot.fromIndex(it.getInt("index"))
                if (slot.valid) equipped[slot] = skill
            }
        level.copyFrom(SkillLevelData.fromNbt(tag.getCompound("level")))
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.run {
            put("learned", NbtList().apply {
                addAll(learned.map {
                    NbtCompound().apply {
                        putString("id", it.key.id.toString())
                        put("data", it.value.toNbt())
                    }
                })
            })
            put("equipped", NbtList().apply {
                addAll(equipped.map {
                    NbtCompound().apply {
                        putString("id", it.value.id.toString())
                        putInt("index", it.key.ordinal)
                    }
                })
            })
            put("level", level.toNbt())
        }
    }

    override fun tick() {
        learned.values.forEach { it.tick() }
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        val oldSkills = learned.filterValues { it.using }.keys.toSet()
        super.applySyncPacket(buf)
        val newSkills = learned.filterValues { it.using }.keys.toSet()
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse() }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop() }
        player.updateScreen()
    }
}