package com.imoonday.component

import com.imoonday.init.ModComponents
import com.imoonday.init.ModSkills
import com.imoonday.network.LearnSkillS2CPacket
import com.imoonday.skill.Skill
import com.imoonday.trigger.CooldownTrigger
import com.imoonday.trigger.UnequipTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.translate
import com.imoonday.util.updateScreen
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.HoverEvent
import net.minecraft.util.Identifier
import kotlin.math.min

interface Skill2IntMapComponent : Component {

    var skills: MutableMap<Skill, Int>
}

class LearnedSkillComponent(private val provider: PlayerEntity) :
    Skill2IntMapComponent,
    AutoSyncedComponent,
    CommonTickingComponent {

    override var skills: MutableMap<Skill, Int> = mutableMapOf()
        get() {
            field.keys.removeIf { it.invalid }
            return field
        }
        set(value) {
            field.clear()
            field.putAll(value)
            field.keys.removeIf { it.invalid }
            ModComponents.SKILLS.sync(provider)
        }

    override fun readFromNbt(tag: NbtCompound) {
        skills = tag.getList("skills", NbtElement.COMPOUND_TYPE.toInt())
            .map { it as NbtCompound }
            .associate { Skill.fromId(Identifier(it.getString("id"))) to it.getInt("cooldown") }
            .toMutableMap()
    }

    override fun writeToNbt(tag: NbtCompound) {
        val list = NbtList()
        list.addAll(skills.map {
            NbtCompound().apply {
                put("id", NbtString.of(it.key.id.toString()))
                putInt("cooldown", it.value)
            }
        })
        tag.put("skills", list)
    }

    override fun tick() {
        var anyFinished = false
        skills.filterValues { it > 0 }.forEach {
            val next = it.value - 1
            skills[it.key] = next
            if (!anyFinished && next <= 0) {
                anyFinished = true
            }
        }
        if (anyFinished && !provider.world.isClient) {
            ModComponents.SKILLS.sync(provider)
        }
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        super.applySyncPacket(buf)
        provider.updateScreen()
    }
}

val PlayerEntity.learnedSkills: Set<Skill>
    get() = getComponent(ModComponents.SKILLS).skills.keys.toSet()

fun PlayerEntity.getCooldown(skill: Skill): Int = getComponent(ModComponents.SKILLS).skills[skill] ?: 0

fun PlayerEntity.isCooling(skill: Skill): Boolean = getCooldown(skill) > 0

fun PlayerEntity.startCooling(skill: Skill, cooldown: Int? = null) {
    if (isCooling(skill)) return
    modifySkills {
        var time = cooldown ?: skill.getCooldown(world)
        equippedSkills.filterIsInstance<CooldownTrigger>()
            .forEach { time = it.getCooldown(time) }
        it[skill] = if (isCreative) min(20, time) else time
        true
    }
}

fun PlayerEntity.stopCooling(skill: Skill) {
    if (!isCooling(skill)) return
    modifySkills {
        it.remove(skill)
        true
    }
}

fun PlayerEntity.modifyCooldown(skill: Skill, operation: (Int) -> Int) {
    modifySkills {
        it[skill] = operation.invoke(getCooldown(skill))
        true
    }
}

fun PlayerEntity.modifySkills(operation: (MutableMap<Skill, Int>) -> Boolean): Boolean {
    val modified = operation(getComponent(ModComponents.SKILLS).skills)
    ModComponents.SKILLS.sync(this)
    ModComponents.EQUIPPED_SKILLS.sync(this)
    return modified
}

fun PlayerEntity.learnSkill(skill: Skill): Boolean =
    modifySkills {
        if (!it.contains(skill)) {
            val skills = equippedSkills
            skills.indexOfFirst { it.invalid }
                .takeIf { it in 0..3 }
                ?.let { skills[it] = skill }
            it[skill] = 0
            (this as? ServerPlayerEntity)?.let { player ->
                ServerPlayNetworking.send(
                    player,
                    LearnSkillS2CPacket(skill)
                )
            }
            sendMessage(translate("learnSkill", "message", skill.name.string).styled {
                it.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        HoverEvent.ItemStackContent(skill.item?.defaultStack ?: Items.AIR.defaultStack)
                    )
                )
            })
            if (it.keys.toSet() == Skill.getValidSkills().toSet())
                sendMessage(translate("learnSkill", "all"))
            true
        } else false
    }

fun PlayerEntity.forgetSkill(skill: Skill): Boolean =
    modifySkills {
        if (it.containsKey(skill)) {
            val skills = equippedSkills
            (0..3).forEach { index ->
                if (skills[index] == skill) {
                    skills[index] = ModSkills.EMPTY
                    (this as? ServerPlayerEntity)?.run {
                        (skill as? UnequipTrigger)?.postUnequipped(this, SkillSlot.fromIndex(index + 1))
                    }
                    stopUsingSkill(skill)
                }
            }
            it.remove(skill)
            sendMessage(translate("forgetSkill", "message", skill.name.string).styled {
                it.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        HoverEvent.ItemStackContent(skill.item?.defaultStack ?: Items.AIR.defaultStack)
                    )
                )
            })
            true
        } else false
    }

fun PlayerEntity.learnRandomSkill(predicate: (Skill) -> Boolean = { true }): Boolean =
    Skill.getValidSkills()
        .filterNot { it in learnedSkills }
        .filter(predicate)
        .takeUnless { it.isEmpty() }
        ?.let { learnSkill(it.random()) } ?: false