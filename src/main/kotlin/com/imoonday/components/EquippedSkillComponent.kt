package com.imoonday.components

import com.imoonday.api.SkillChangeEvents
import com.imoonday.init.ModComponents
import com.imoonday.network.EquipSkillC2SRequest
import com.imoonday.skills.Skills
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillSlot
import com.imoonday.utils.updateScreen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList

interface SkillListComponent : Component {
    var skills: DefaultedList<Skill>
}

class EquippedSkillComponent(private val provider: PlayerEntity) : SkillListComponent, AutoSyncedComponent {
    override var skills: DefaultedList<Skill> = DefaultedList.ofSize(4, Skills.EMPTY)
        set(value) {
            field = value
            ModComponents.EQUIPPED_SKILLS.sync(provider)
        }

    override fun readFromNbt(tag: NbtCompound) {
        skills = DefaultedList.ofSize<Skill>(4, Skills.EMPTY).apply {
            tag.getList("skills", NbtElement.COMPOUND_TYPE.toInt()).forEach { skillTag ->
                (skillTag as NbtCompound).run {
                    val index = getInt("index")
                    if (index in 0 until 4) {
                        this@apply[index] = Skills.get(Identifier(getString("id")))
                    }
                }
            }
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("skills", NbtList().apply {
            skills.forEachIndexed { index, skill ->
                add(NbtCompound().apply {
                    putInt("index", index)
                    putString("id", skill.id.toString())
                })
            }
        })
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        super.applySyncPacket(buf)
        provider.updateScreen()
    }
}

val PlayerEntity.equippedSkills: DefaultedList<Skill>
    get() = getComponent(ModComponents.EQUIPPED_SKILLS).skills

fun PlayerEntity.equipSkill(slot: SkillSlot, skill: Skill): Boolean {
    if (world.isClient) {
        ClientPlayNetworking.send(EquipSkillC2SRequest(slot, skill))
    } else {
        if (!skill.isEmpty && skill !in learnedSkills) return false
        val player = this as ServerPlayerEntity
        val skills = equippedSkills
        if (skills[slot.ordinal - 1] == skill) {
            ModComponents.EQUIPPED_SKILLS.sync(this)
            return false
        }

        val oldSkill = skills[slot.ordinal - 1]
        var move = false
        if (!skill.isEmpty) {
            (0..3).forEach {
                if (skills[it] == skill) {
                    skills[it] = Skills.EMPTY
                    move = true
                }
            }
        }
        if (!move) {
            if (skill.isEmpty) {
                if (!SkillChangeEvents.UNEQUIPPED.invoker().onUnequipped(player, slot, oldSkill)) {
                    ModComponents.EQUIPPED_SKILLS.sync(this)
                    return false
                }
            } else if (!SkillChangeEvents.UNEQUIPPED.invoker()
                    .onUnequipped(player, slot, oldSkill) || !SkillChangeEvents.EQUIPPED.invoker()
                    .onEquipped(player, slot, skill)
            ) {
                ModComponents.EQUIPPED_SKILLS.sync(this)
                return false
            }
        }
        skills[slot.ordinal - 1] = skill
        if (!move) {
            if (skill.isEmpty) {
                SkillChangeEvents.POST_UNEQUIPPED.invoker().postUnequipped(player, slot, oldSkill)
            } else {
                SkillChangeEvents.POST_EQUIPPED.invoker().postEquipped(player, slot, skill)
                if (!oldSkill.isEmpty) SkillChangeEvents.POST_UNEQUIPPED.invoker()
                    .postUnequipped(player, slot, oldSkill)
            }
        }
        ModComponents.EQUIPPED_SKILLS.sync(this)
    }
    return true
}

fun PlayerEntity.getSkill(slot: SkillSlot) = if (slot.valid) equippedSkills[slot.ordinal - 1] else Skills.EMPTY