package com.imoonday.util

import com.imoonday.api.SkillChangeEvents
import com.imoonday.component.DataComponent
import com.imoonday.init.ModComponents
import com.imoonday.init.ModSkills
import com.imoonday.network.EquipSkillC2SRequest
import com.imoonday.network.LearnSkillS2CPacket
import com.imoonday.skill.Skill
import com.imoonday.trigger.CooldownTrigger
import com.imoonday.trigger.SkillTrigger
import com.imoonday.trigger.SkillTriggerHandler
import com.imoonday.trigger.UnequipTrigger
import com.imoonday.util.PlayerUtils.getNextLevelExp
import com.imoonday.util.PlayerUtils.shouldLearnSkill
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.HoverEvent
import net.minecraft.util.Util
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.*

object PlayerUtils {

    private val levelExpCache = IntArray(101) { -1 }
    fun getNextLevelExp(level: Int): Int =
        if (levelExpCache[level] >= 0) {
            levelExpCache[level]
        } else when {
            level >= 30 -> 112 + (level - 30) * 9
            level >= 15 -> 37 + (level - 15) * 5
            else -> 7 + level * 2
        }.also { levelExpCache[level] = it }

    fun shouldLearnSkill(level: Int): Boolean = when {
        level <= 0 -> false
        level in 1..14 -> level % 5 == 0
        level in 15..29 -> level % 3 == 0
        level in 30..80 -> level % 2 == 0
        else -> true
    }
}

private val PlayerEntity.data: DataComponent
    get() = getComponent(ModComponents.DATA)
val PlayerEntity.learnedSkills: Set<Skill>
    get() = data.learned.keys.filterNot { it.invalid }.toSet()

fun PlayerEntity.syncData() {
    if (!world.isClient) ModComponents.DATA.sync(this)
}

fun PlayerEntity.hasLearned(skill: Skill): Boolean = skill in learnedSkills

fun PlayerEntity.getCooldown(skill: Skill): Int = data.learned[skill]?.cooldown ?: 0
fun PlayerEntity.isCooling(skill: Skill): Boolean = getCooldown(skill) > 0
fun PlayerEntity.startCooling(skill: Skill, cooldown: Int? = null) {
    if (isCooling(skill)) return
    modifySkills {
        var time = cooldown ?: skill.getCooldown(world)
        getTriggers<CooldownTrigger>().forEach { trigger -> time = trigger.getCooldown(time) }
        it[skill]?.cooldown = if (isCreative) min(20, time) else time
        true
    }
}

fun PlayerEntity.stopCooling(skill: Skill) {
    if (!isCooling(skill)) return
    modifySkills {
        it[skill]?.cooldown = 0
        true
    }
}

fun PlayerEntity.modifyCooldown(skill: Skill, operation: (Int) -> Int) {
    modifySkills {
        it[skill]?.cooldown = operation.invoke(getCooldown(skill))
        true
    }
}

fun PlayerEntity.modifySkills(operation: (MutableMap<Skill, SkillData>) -> Boolean): Boolean {
    val modified = operation(data.learned)
    syncData()
    return modified
}

fun PlayerEntity.learn(skill: Skill): Boolean =
    modifySkills { map ->
        if (!map.contains(skill)) {
            map[skill] = SkillData()
            val skills = data.equipped
            skills.filterValues { it.invalid }.keys
                .firstOrNull()
                ?.let { skills[it] = skill }
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
            if (map.keys.toSet() == Skill.getValidSkills().toSet())
                sendMessage(translate("learnSkill", "all"))
            true
        } else false
    }

fun PlayerEntity.forget(skill: Skill): Boolean =
    modifySkills { map ->
        if (map.containsKey(skill)) {
            val skills = data.equipped
            skills.filterValues { it == skill }.forEach {
                val slot = it.key
                skills[slot] = ModSkills.EMPTY
                (this as? ServerPlayerEntity)?.run {
                    (skill as? UnequipTrigger)?.postUnequipped(this, slot)
                }
                stopUsing(skill)
            }
            map.remove(skill)
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

fun PlayerEntity.learnRandomly(predicate: (Skill) -> Boolean = { true }): Boolean =
    Skill.getValidSkills()
        .filterNot { hasLearned(it) }
        .filter(predicate)
        .takeUnless { it.isEmpty() }
        ?.let { learn(it.random()) } ?: false

val PlayerEntity.equippedSkills: List<Skill>
    get() = data.equipped.values.toList()

fun PlayerEntity.hasEquipped(skill: Skill): Boolean = skill in equippedSkills

fun PlayerEntity.equip(skill: Skill, slot: SkillSlot): Boolean {
    if (world.isClient) {
        ClientPlayNetworking.send(EquipSkillC2SRequest(slot, skill))
    } else {
        if (!skill.invalid && !hasLearned(skill)) return false
        val player = this as ServerPlayerEntity
        val skills = data.equipped
        if (skills[slot] == skill) {
            syncData()
            return false
        }
        val oldSkill = skills[slot]
        var move = false
        if (!skill.invalid) {
            skills.forEach {
                if (it.value == skill) {
                    skills[it.key] = ModSkills.EMPTY
                    move = true
                }
            }
        }
        if (!move) {
            if (skill.invalid) {
                if (!SkillChangeEvents.UNEQUIPPED.invoker().onUnequipped(player, slot, oldSkill)) {
                    syncData()
                    return false
                }
            } else if (!SkillChangeEvents.UNEQUIPPED.invoker()
                    .onUnequipped(player, slot, oldSkill) || !SkillChangeEvents.EQUIPPED.invoker()
                    .onEquipped(player, slot, skill)
            ) {
                syncData()
                return false
            }
        }
        skills[slot] = skill
        if (!move) {
            if (skill.invalid) {
                SkillChangeEvents.POST_UNEQUIPPED.invoker().postUnequipped(player, slot, oldSkill)
            } else {
                SkillChangeEvents.POST_EQUIPPED.invoker().postEquipped(player, slot, skill)
                if (!oldSkill!!.invalid) SkillChangeEvents.POST_UNEQUIPPED.invoker()
                    .postUnequipped(player, slot, oldSkill)
            }
        }
        syncData()
    }
    return true
}

fun PlayerEntity.getSkill(slot: SkillSlot) = data.equipped[slot] ?: ModSkills.EMPTY

var PlayerEntity.skillExp: Long
    get() {
        updateLevel()
        return data.level.experience
    }
    set(value) {
        data.level.experience = value
        updateLevel()
        syncData()
    }
var PlayerEntity.skillLevel: Int
    get() {
        updateCycle()
        return data.level.level
    }
    set(value) {
        data.level.level = value
        updateCycle()
        syncData()
    }

private fun PlayerEntity.updateLevel() {
    updateCycle()
    var needed: Int
    while (data.level.experience >= getNextLevelExp(data.level.level).also { needed = it }) {
        data.level.experience -= needed
        data.level.level++
        updateCycle()
        if (shouldLearnSkill(data.level.level)) learnRandomly()
    }
}

private fun PlayerEntity.updateCycle() {
    while (data.level.level > 100) {
        data.level.level -= 101
        data.level.cycle++
    }
}

val PlayerEntity.usingSkills: Set<Skill>
    get() = data.learned.filterValues { it.using }.keys.toSet()

fun PlayerEntity.startUsing(skill: Skill, data: NbtCompound? = null): Boolean {
    if (skill in usingSkills) return false
    if (skill.invalid) return false
    this.data.learned[skill]?.apply {
        using = true
        usedTime = 0
        data?.let { this.data.copyFrom(it) }
    }
    syncData()
    return true
}

fun PlayerEntity.stopUsing(skill: Skill): Boolean {
    if (skill !in usingSkills) return false
    this.data.learned[skill]?.apply {
        using = false
        data = NbtCompound()
        SkillTriggerHandler.postStop(this@stopUsing)
    }
    syncData()
    return true
}

fun PlayerEntity.toggleUsing(skill: Skill, data: NbtCompound? = null): Boolean {
    return if (skill in usingSkills) {
        stopUsing(skill)
        false
    } else startUsing(skill, data)
}

fun PlayerEntity.isUsing(skill: Skill) = skill in usingSkills
fun PlayerEntity.getUsedTime(skill: Skill): Int = getData(skill)?.usedTime ?: 0

fun PlayerEntity.modifyUsedTime(skill: Skill, operation: (Int) -> Int) {
    getData(skill)?.usedTime = operation(getUsedTime(skill))
    syncData()
}

fun PlayerEntity.resetUsedTime(skill: Skill) {
    modifyUsedTime(skill) { 0 }
    syncData()
}

fun PlayerEntity.getUsingData(skill: Skill): NbtCompound? = getData(skill)?.data

fun PlayerEntity.getData(skill: Skill): SkillData? = data.learned[skill]

var ServerPlayerEntity.lastDamagedTime: Long
    get() = data.extraData.getLong("lastDamagedTime")
    set(value) = data.extraData.putLong("lastDamagedTime", value)
var ServerPlayerEntity.lastReflectedTime: Long
    get() = data.extraData.getLong("lastReflectedTime")
    set(value) = data.extraData.putLong("lastReflectedTime", value)

fun ServerPlayerEntity.onDamage() {
    lastDamagedTime = Util.getMeasuringTimeMs()
    val l = lastDamagedTime - lastReflectedTime
    if (l < 1000) {
        sendMessage(translateSkill("extreme_reflection", "early", (l / 1000.0).toString()), true)
        lastReflectedTime = 0
        lastDamagedTime = 0
    }
}

fun PlayerEntity.raycastVisualBlock(maxDistance: Double): HitResult {
    val vec3d: Vec3d = getCameraPosVec(0f)
    val vec3d2: Vec3d = getRotationVec(0f)
    val vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance)
    return world.raycast(
        RaycastContext(
            vec3d,
            vec3d3,
            RaycastContext.ShapeType.VISUAL,
            RaycastContext.FluidHandling.NONE,
            this
        )
    )
}

inline fun <reified T : SkillTrigger> PlayerEntity.getTriggers(): List<T> =
    equippedSkills.filterIsInstance<T>()

/**
 * @return The angle between the player and the given position, between 0 to π
 * */
fun PlayerEntity.calculateAngle(pos: Vec3d): Double {
    val vectorX = pos.x - x
    val vectorY = pos.y - eyeY
    val vectorZ = pos.z - z
    val vector = rotationVector.normalize()
    val pitchRadians = Math.toRadians(pitch.toDouble())
    val adjustedY = vectorY - tan(pitchRadians)
    val adjustedMagnitude = sqrt(vectorX.pow(2) + adjustedY.pow(2) + vectorZ.pow(2))
    val adjustedProduct = vectorX * vector.x + adjustedY * vector.y + vectorZ * vector.z
    val adjustedAngle = acos(adjustedProduct / adjustedMagnitude)
    return adjustedAngle
}

val PlayerEntity.skillInitialized: Boolean
    get() = try {
        ModComponents.DATA.maybeGet(this).isPresent
    } catch (e: Exception) {
        false
    }