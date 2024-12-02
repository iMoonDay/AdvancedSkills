package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.PlayerUtils.getNextLevelExp
import com.imoonday.advskills_re.util.PlayerUtils.shouldLearnSkill
import net.minecraft.entity.player.*
import net.minecraft.entity.projectile.*
import net.minecraft.item.*
import net.minecraft.nbt.*
import net.minecraft.network.listener.*
import net.minecraft.network.packet.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*
import net.minecraft.world.*
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

    fun getLevelRequiredForLearningSkill(currentLevel: Int): Int {
        var level = currentLevel + 1
        while (true) {
            if (level > 100) {
                level -= 100
            }
            if (shouldLearnSkill(level)) {
                return level
            }
            level++
        }
    }
}

val PlayerEntity.data: PlayerDataComponent
    get() = (this as PlayerDataContainer).dataComponent
val PlayerEntity.skillContainer: SkillContainer
    get() = data.container
val PlayerEntity.learnedSkills: Set<Skill>
    get() = skillContainer.getAllSkills()

fun PlayerEntity.syncData(force: Boolean = true) {
    if (this is ServerPlayerEntity) {
        if (force) {
            this.data.sync()
        } else {
            this.data.dirty = true
        }
    }
}

fun PlayerEntity.resetData() {
    data.reset()
    data.dirty = true
    properties = NbtCompound()
}

fun PlayerEntity.copyDataFrom(other: PlayerEntity) {
    data.readFromNbt(other.data.toNbt())
}

fun PlayerEntity.getSlot(index: Int): SkillSlot? = skillContainer.getSlot(index)

fun PlayerEntity.getSlot(slot: SkillSlot): SkillSlot? = skillContainer.getSlot(slot.index)

fun PlayerEntity.hasLearned(skill: Skill): Boolean = skill in learnedSkills

fun PlayerEntity.getCooldown(skill: Skill): Int = skillContainer.getData(skill)?.cooldown ?: 0
fun PlayerEntity.isCooling(skill: Skill): Boolean = getCooldown(skill) > 0
fun PlayerEntity.startCooling(skill: Skill, cooldown: Int? = null) {
    if (isCooling(skill)) return
    modifySkillData(skill) {
        var time = cooldown ?: skill.cooldown
        forEachTrigger<CooldownTrigger> { trigger -> time = trigger.getCooldown(time) }
        it.cooldown = if (isCreative) min(20, time) else time
        true
    }
}

fun PlayerEntity.stopCooling(skill: Skill) {
    if (!isCooling(skill)) return
    modifySkillData(skill) {
        it.cooldown = 0
        true
    }
}

fun PlayerEntity.modifyCooldown(skill: Skill, operation: (Int) -> Int) {
    modifySkillData(skill) {
        it.cooldown = operation(getCooldown(skill))
        true
    }
}

fun PlayerEntity.modifySkillData(skill: Skill, operation: (SkillData) -> Boolean): Boolean =
    skillContainer.getData(skill)?.run {
        val modified = operation(this)
        syncData()
        return modified
    } ?: false

fun PlayerEntity.learn(skill: Skill, toast: Boolean = true, message: Boolean = true): Boolean =
    skillContainer.learn(skill) { result ->
        if (result) {
            skillContainer.getEmptySlot(skill)?.equip(skill)
            (this as? ServerPlayerEntity)?.let {
                Channels.LEARN_SKILL_S2C.sendToPlayer(it, LearnSkillS2CPacket(skill, toast))
            }
            if (message) {
                sendMessage(translate("learnSkill.message", skill.name).styled {
                    it.withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_ITEM,
                            HoverEvent.ItemStackContent(skill.item?.defaultStack ?: Items.AIR.defaultStack)
                        )
                    )
                })
                if (skillContainer.getAllSkills().size == Skills.getValidSkills().size)
                    sendMessage(translate("learnSkill.all"))
            }
        }
        syncData()
    }

fun PlayerEntity.learnAll() {
    if (skillContainer.getAllSkills().size != Skills.getValidSkills().size) {
        skillContainer.learnAll {
            skillContainer.getEmptySlot(it)?.equip(it)
        }
        syncData()
    }
    sendMessage(translate("learnSkill.all"))
}

fun PlayerEntity.forget(skill: Skill, message: Boolean = true): Boolean =
    skillContainer.forget(skill, { result ->
        if (result && message) {
            sendMessage(translate("forgetSkill.message", skill.name).styled {
                it.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        HoverEvent.ItemStackContent(skill.item?.defaultStack ?: Items.AIR.defaultStack)
                    )
                )
            })
        }
        syncData()
    }) { slot, result ->
        if (result && this is ServerPlayerEntity && slot is UnequipTrigger) {
            slot.postUnequipped(this, slot)
            stopUsing(skill)
        }
    }

fun PlayerEntity.forgetAll() {
    if (skillContainer.getAllSkills().isNotEmpty()) {
        skillContainer.forgetAll {
            if (this is ServerPlayerEntity && it is UnequipTrigger) {
                it.postUnequipped(this, it)
                stopUsing(it.skill)
            }
        }
        syncData()
    }
    sendMessage(translate("forgetSkill.all"))
}

fun PlayerEntity.learnRandomly(filter: (Skill) -> Boolean = { true }): Boolean =
    Skills.random { !hasLearned(it) && filter(it) }
        .takeUnless { it.isEmpty() }
        ?.let { learn(it) } ?: false

val PlayerEntity.learnableData: LearnableSkillData
    get() = data.learnable

fun ServerPlayerEntity.addChoice() {
    learnableData.count++
}

fun PlayerEntity.getChoice(): SkillChoice = learnableData.get()

fun PlayerEntity.refreshChoice(force: Boolean = false) = if (this is ServerPlayerEntity) {
    learnableData.refresh(force, learnedSkills)
    syncData()
} else {
    Channels.REFRESH_CHOICE_C2S.sendToServer(RefreshChoiceC2SRequest())
}

fun PlayerEntity.canFreshChoice(): Boolean =
    SkillChoice.canGenerate(learnedSkills) && !learnableData.refreshed

fun PlayerEntity.choose(id: Int): Boolean = when (id) {
    0 -> chooseFirst()
    1 -> chooseSecond()
    2 -> chooseThird()
    else -> false
}

private fun ServerPlayerEntity.choose(index: Int): Boolean {
    if (learnableData.isEmpty()) return false
    learnableData.run {
        val skill = when (index) {
            0 -> first
            1 -> second
            2 -> third
            else -> return false
        }
        if (skill.invalid) {
            correct(learnedSkills)
            return false
        }
        learn(skill)
        next(learnedSkills)
        syncData()
    }
    return true
}

fun PlayerEntity.chooseFirst(): Boolean = if (this is ServerPlayerEntity) choose(0) else {
    Channels.CHOOSE_SKILL_C2S.sendToServer(ChooseSkillC2SRequest(0))
    true
}

fun PlayerEntity.chooseSecond(): Boolean = if (this is ServerPlayerEntity) choose(1) else {
    Channels.CHOOSE_SKILL_C2S.sendToServer(ChooseSkillC2SRequest(1))
    true
}

fun PlayerEntity.chooseThird(): Boolean = if (this is ServerPlayerEntity) choose(2) else {
    Channels.CHOOSE_SKILL_C2S.sendToServer(ChooseSkillC2SRequest(2))
    true
}

val PlayerEntity.equippedSkills: List<Skill>
    get() = skillContainer.getAllSlots().map { it.skill }

fun PlayerEntity.hasEquipped(skill: Skill): Boolean = skill in equippedSkills

fun PlayerEntity.equip(skill: Skill): Boolean =
    skillContainer.getEmptySlot(skill)?.let { equip(skill, it.index) } ?: false

fun PlayerEntity.equip(skill: Skill, slot: SkillSlot): Boolean = equip(skill, slot.index)

fun PlayerEntity.equip(skill: Skill, index: Int): Boolean {
    if (world.isClient) {
        Channels.EQUIP_SKILL_C2S.sendToServer(EquipSkillC2SRequest(index, skill))
        return true
    } else if (this is ServerPlayerEntity) {
        if (skill.invalid && !skill.isEmpty()) return false
        if (!skill.invalid && !hasLearned(skill)) return false
        val slot = skillContainer.getSlot(index) ?: return false
        if (slot.skill == skill || !slot.canEquip(skill)) return false
        val original = slot.skill
        var move = false
        if (!skill.invalid) skillContainer.getSlot(skill)?.let { it.unequip { move = true } }
        if (!move) {
            if (skill.invalid) {
                if (SkillChangeEvents.UNEQUIPPED.invoker().onUnequipped(this, slot, original).isFalse) {
                    syncData()
                    return false
                }
            } else if (SkillChangeEvents.UNEQUIPPED.invoker().onUnequipped(this, slot, original).isFalse
                || SkillChangeEvents.EQUIPPED.invoker().onEquipped(this, slot, skill).isFalse
            ) {
                syncData()
                return false
            }
        }
        slot.equip(skill) { syncData() }
        if (!move) {
            if (skill.invalid) {
                SkillChangeEvents.POST_UNEQUIPPED.invoker().postUnequipped(this, slot, original)
            } else {
                SkillChangeEvents.POST_EQUIPPED.invoker().postEquipped(this, slot, skill)
                if (!original.invalid) SkillChangeEvents.POST_UNEQUIPPED.invoker()
                    .postUnequipped(this, slot, original)
            }
        }
        syncData()
        return true
    }
    return false
}

fun PlayerEntity.getSkill(slot: Int) = skillContainer.getSlot(slot)?.skill ?: Skills.EMPTY

fun PlayerEntity.getSkill(slot: SkillSlot) = skillContainer.getSlot(slot.index)?.skill ?: Skills.EMPTY

var PlayerEntity.skillExp: Int
    get() {
        updateLevel()
        return levelData.experience
    }
    set(value) {
        levelData.experience = value
        updateLevel()
        syncData()
    }
var PlayerEntity.skillLevel: Int
    get() {
        updateCycle()
        return levelData.level
    }
    set(value) {
        levelData.level = value
        updateCycle()
        syncData()
    }
var PlayerEntity.skillCycle: Int
    get() {
        updateCycle()
        return levelData.cycle
    }
    set(value) {
        levelData.cycle = value
        syncData()
    }
val PlayerEntity.levelData: SkillLevelData
    get() = data.level

private fun PlayerEntity.updateLevel() {
    updateCycle()
    var needed: Int
    var added = false
    while (levelData.experience >= getNextLevelExp(levelData.level).also { needed = it }) {
        levelData.experience -= needed
        levelData.level++
        updateCycle()
        if (shouldLearnSkill(levelData.level) && this is ServerPlayerEntity) {
            addChoice()
            added = true
        }
    }
    if (added && this is ServerPlayerEntity) {
        sendMessage(translate("skillLevel.addChoice"))
        playSound(SoundEvents.ENTITY_PLAYER_LEVELUP)
        syncData()
    }
}

private fun PlayerEntity.updateCycle() {
    while (levelData.level > 100) {
        levelData.level -= 101
        levelData.cycle++
    }
}

val PlayerEntity.usingSkills: Set<Skill>
    get() = skillContainer.getAllSkills { _, data -> data.using }

fun PlayerEntity.startUsing(skill: Skill, data: NbtCompound? = null): Boolean {
    if (skill in usingSkills) return false
    if (skill.invalid) return false
    getData(skill)?.apply {
        using = true
        usedTime = 0
        data?.let { this.data.copyFrom(it) }
    }
    syncData()
    return true
}

fun PlayerEntity.stopUsing(skill: Skill): Boolean {
    if (skill !in usingSkills) return false
    getData(skill)?.apply {
        using = false
        data = NbtCompound()
        SkillTriggerHandler.postStop(this@stopUsing)
    }
    syncData()
    return true
}

fun PlayerEntity.toggleUsing(skill: Skill, data: NbtCompound? = null): Boolean =
    if (skill in usingSkills) {
        stopUsing(skill)
        false
    } else startUsing(skill, data)

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

fun PlayerEntity.getData(skill: Skill): SkillData? = skillContainer.getData(skill)

fun PlayerEntity.isCharging(skill: Skill): Boolean = skill is LongPressTrigger && isUsing(skill)

var ServerPlayerEntity.lastDamagedTime: Long
    get() = properties.getLong("lastDamagedTime")
    set(value) {
        properties.putLong("lastDamagedTime", value)
        syncProperties()
    }
var ServerPlayerEntity.lastReflectedTime: Long
    get() = properties.getLong("lastReflectedTime")
    set(value) {
        properties.putLong("lastReflectedTime", value)
        syncProperties()
    }

fun ServerPlayerEntity.onDamage() {
    if (equippedSkills.none { it is ReflectionTrigger }) return
    lastDamagedTime = System.currentTimeMillis()
    val l = lastDamagedTime - lastReflectedTime
    if (l < 1000) {
        sendMessage(translate("reflection.early", (l / 1000.0).toString()), true)
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

inline fun <reified T : SkillTrigger> PlayerEntity.forEachTrigger(
    filter: (T) -> Boolean = { true },
    action: (T) -> Unit
) = getTriggers<T>().filter(filter).forEach(action)

inline fun <reified T : SkillTrigger> PlayerEntity.anyTrigger(mapper: (T) -> Boolean): Boolean =
    getTriggers<T>().map(mapper).any { it }

inline fun <reified T : SkillTrigger> PlayerEntity.allTriggers(mapper: (T) -> Boolean): Boolean =
    getTriggers<T>().map(mapper).all { it }

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

fun PlayerEntity.raycastLivingEntity(distance: Double): EntityHitResult? {
    val cameraPos = getCameraPosVec(0f)
    return ProjectileUtil.raycast(
        this,
        cameraPos,
        cameraPos.add(rotationVector.multiply(distance)),
        boundingBox.stretch(rotationVector.multiply(distance)).expand(1.0),
        { !it.isSpectator && it.isAlive && it.isLiving },
        distance * distance
    )
}

fun PlayerEntity.sendPacket(packet: Packet<out PacketListener>) = if (this is ServerPlayerEntity) {
    networkHandler.sendPacket(packet)
} else {
    ClientUtils.sendToServer(packet)
}

fun ServerPlayerEntity.spawnParticles(
    type: ParticleEffect,
    force: Boolean,
    pos: Vec3d,
    count: Int,
    deltaX: Double,
    deltaY: Double,
    deltaZ: Double,
    speed: Double,
) {
    if (force) {
        val particleS2CPacket = ParticleS2CPacket(
            type,
            true,
            pos.x,
            pos.y,
            pos.z,
            deltaX.toFloat(),
            deltaY.toFloat(),
            deltaZ.toFloat(),
            speed.toFloat(),
            count
        )
        serverWorld.players.forEach {
            serverWorld.sendToPlayerIfNearby(
                it,
                true,
                pos.x,
                pos.y,
                pos.z,
                particleS2CPacket
            )
        }
    } else {
        serverWorld.spawnParticles(type, pos.x, pos.y, pos.z, count, deltaX, deltaY, deltaZ, speed)
    }
}

fun ServerPlayerEntity.playSound(sound: SoundEvent) = world.playSound(null, blockPos, sound, SoundCategory.PLAYERS)

fun ServerPlayerEntity.addTask(task: LoopTask): Int = (this as LoopTaskContainer).addTask(task)

fun ServerPlayerEntity.removeTask(id: Int) = (this as LoopTaskContainer).removeTask(id)

fun ServerPlayerEntity.getTask(id: Int): LoopTask? = (this as LoopTaskContainer).getTask(id)

fun ServerPlayerEntity.clearTasks() = (this as LoopTaskContainer).clearTasks()

fun ServerPlayerEntity.getTasks(): List<LoopTask> = (this as LoopTaskContainer).tasks