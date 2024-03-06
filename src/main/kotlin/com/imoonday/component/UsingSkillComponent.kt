package com.imoonday.component

import com.imoonday.init.ModComponents
import com.imoonday.init.ModSkills
import com.imoonday.skill.Skill
import com.imoonday.trigger.ClientUseTrigger
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

interface Skill2NbtComponent : Component {
    var skills: MutableMap<Skill, NbtCompound>
}

class UsingSkillComponent(private val provider: PlayerEntity) : Skill2NbtComponent, AutoSyncedComponent {
    override var skills: MutableMap<Skill, NbtCompound> = mutableMapOf()
        get() {
            field.keys.removeIf { it.invalid }
            return field
        }
        set(value) {
            field.clear()
            field.putAll(value)
            field.keys.removeIf { it.invalid }
            ModComponents.USING_SKILLS.sync(provider)
        }

    override fun readFromNbt(tag: NbtCompound) {
        skills = tag.getList("skills", NbtElement.COMPOUND_TYPE.toInt())
            .map { it as NbtCompound }
            .associate { ModSkills.get(Identifier(it.getString("id"))) to it.getCompound("data") }
            .toMutableMap()
    }

    override fun writeToNbt(tag: NbtCompound) {
        val list = NbtList()
        list.addAll(skills.map {
            NbtCompound().apply {
                put("id", NbtString.of(it.key.id.toString()))
                put("data", it.value)
            }
        })
        tag.put("skills", list)
    }

    override fun applySyncPacket(buf: PacketByteBuf?) {
        val oldSkills = skills.keys.toSet()
        super.applySyncPacket(buf)
        val newSkills = skills.keys.toSet()
        newSkills.subtract(oldSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onUse(MinecraftClient.getInstance()) }
        oldSkills.subtract(newSkills)
            .filterIsInstance<ClientUseTrigger>()
            .forEach { it.onStop(MinecraftClient.getInstance()) }
    }
}

val PlayerEntity.usingSkills: Set<Skill>
    get() = getComponent(ModComponents.USING_SKILLS).skills.keys.toSet()

fun PlayerEntity.startUsingSkill(skill: Skill, data: NbtCompound? = null): Boolean {
    if (skill in usingSkills) return false
    if (skill.invalid) return false
    getComponent(ModComponents.USING_SKILLS).skills[skill] = NbtCompound().apply {
        putInt("time", 0)
        if (data != null) put("data", data)
    }
    ModComponents.USING_SKILLS.sync(this)
    return true
}

fun PlayerEntity.stopUsingSkill(skill: Skill): Boolean {
    val result = getComponent(ModComponents.USING_SKILLS).skills.keys.remove(skill)
    ModComponents.USING_SKILLS.sync(this)
    return result
}

fun PlayerEntity.toggleUsingSkill(skill: Skill): Boolean {
    return if (skill in usingSkills) {
        stopUsingSkill(skill)
        false
    } else startUsingSkill(skill)
}

fun PlayerEntity.isUsingSkill(skill: Skill) = skill in usingSkills

fun PlayerEntity.getSkillUsedTime(skill: Skill): Int =
    getComponent(ModComponents.USING_SKILLS).skills[skill]?.getInt("time") ?: 0

fun PlayerEntity.updateSkillUsedTime() {
    val skills = getComponent(ModComponents.USING_SKILLS).skills
    skills.forEach { skills[it.key] = it.value.apply { putInt("time", it.value.getInt("time") + 1) } }
    ModComponents.USING_SKILLS.sync(this)
}

fun PlayerEntity.getSkillData(skill: Skill): NbtCompound? =
    getComponent(ModComponents.USING_SKILLS).skills[skill]?.getCompound("data")