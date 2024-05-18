package com.imoonday.util

import com.imoonday.api.SkillChangeEvents
import com.imoonday.component.Components
import com.imoonday.component.DataComponent
import com.imoonday.component.properties
import com.imoonday.network.*
import com.imoonday.skill.Skill
import com.imoonday.trigger.*
import com.imoonday.util.PlayerUtils.getNextLevelExp
import com.imoonday.util.PlayerUtils.shouldLearnSkill
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.PacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.HoverEvent
import net.minecraft.util.Util
import net.minecraft.util.hit.EntityHitResult
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
    get() = getComponent(Components.DATA)
val PlayerEntity.skillContainer: SkillContainer
    get() = data.container
val PlayerEntity.learnedSkills: Set<Skill>
    get() = skillContainer.getAllSkills()

fun PlayerEntity.syncData() {
    if (!world.isClient) Components.DATA.sync(this)
}

fun PlayerEntity.resetData() {
    data.reset()
    properties = NbtCompound()
}

fun PlayerEntity.getSlot(index: Int): SkillSlot? = skillContainer.getSlot(index)

fun PlayerEntity.getSlot(slot: SkillSlot): SkillSlot? = skillContainer.getSlot(slot.index)

fun PlayerEntity.hasLearned(skill: Skill): Boolean = skill in learnedSkills

fun PlayerEntity.getCooldown(skill: Skill): Int = skillContainer.getData(skill)?.cooldown ?: 0
fun PlayerEntity.isCooling(skill: Skill): Boolean = getCooldown(skill) > 0
fun PlayerEntity.startCooling(skill: Skill, cooldown: Int? = null) {
    if (isCooling(skill)) return
    modifySkillData(skill) {
        var time = cooldown ?: skill.getCooldown(world)
        getTriggers<CooldownTrigger>().forEach { trigger -> time = trigger.getCooldown(time) }
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
                ServerPlayNetworking.send(it, LearnSkillS2CPacket(skill, toast))
            }
            if (message) {
                sendMessage(translate("learnSkill", "message", skill.name.string).styled {
                    it.withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_ITEM,
                            HoverEvent.ItemStackContent(skill.item?.defaultStack ?: Items.AIR.defaultStack)
                        )
                    )
                })
                if (skillContainer.getAllSkills().size == Skill.getValidSkills().size)
                    sendMessage(translate("learnSkill", "all"))
            }
        }
        syncData()
    }

fun PlayerEntity.learnAll(toast: Boolean = false) =
    Skill.getLearnableSkills(learnedSkills)
        .forEach { learn(it, toast, false) }
        .also { sendMessage(translate("learnSkill", "all")) }

fun PlayerEntity.forget(skill: Skill, message: Boolean = true): Boolean =
    skillContainer.forget(skill, { result ->
        if (result && message) {
            sendMessage(translate("forgetSkill", "message", skill.name.string).styled {
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

fun PlayerEntity.forgetAll() =
    learnedSkills.forEach { forget(it, false) }.also { sendMessage(translate("forgetSkill", "all")) }

fun PlayerEntity.learnRandomly(filter: (Skill) -> Boolean = { true }): Boolean =
    Skill.getValidSkills()
        .filterNot { hasLearned(it) }
        .filter(filter)
        .takeUnless { it.isEmpty() }
        ?.let { learn(it.random()) } ?: false

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
    ClientPlayNetworking.send(RefreshChoiceC2SRequest())
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
    ClientPlayNetworking.send(ChooseSkillC2SRequest(0))
    true
}

fun PlayerEntity.chooseSecond(): Boolean = if (this is ServerPlayerEntity) choose(1) else {
    ClientPlayNetworking.send(ChooseSkillC2SRequest(1))
    true
}

fun PlayerEntity.chooseThird(): Boolean = if (this is ServerPlayerEntity) choose(2) else {
    ClientPlayNetworking.send(ChooseSkillC2SRequest(2))
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
        ClientPlayNetworking.send(EquipSkillC2SRequest(index, skill))
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
                if (!SkillChangeEvents.UNEQUIPPED.invoker().onUnequipped(this, slot, original)) {
                    syncData()
                    return false
                }
            } else if (!SkillChangeEvents.UNEQUIPPED.invoker().onUnequipped(this, slot, original)
                || !SkillChangeEvents.EQUIPPED.invoker().onEquipped(this, slot, skill)
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

fun PlayerEntity.getSkill(slot: Int) = skillContainer.getSlot(slot)?.skill ?: Skill.EMPTY

fun PlayerEntity.getSkill(slot: SkillSlot) = skillContainer.getSlot(slot.index)?.skill ?: Skill.EMPTY

fun ClientPlayerEntity.requestUse(
    index: Int,
    keyState: UseSkillC2SRequest.KeyState,
) {
    ClientPlayNetworking.send(
        UseSkillC2SRequest(
            index,
            keyState,
            NbtCompound().apply {
                (getSkill(index) as? SendPlayerDataTrigger)
                    ?.takeIf { it.getSendTime() == SendTime.USE }
                    ?.write(this@requestUse, this)
            }
        )
    )
}

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
        sendMessage(translate("skillLevel", "addChoice"))
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

var ServerPlayerEntity.lastDamagedTime: Long
    get() = properties.getLong("lastDamagedTime")
    set(value) = properties.putLong("lastDamagedTime", value)
var ServerPlayerEntity.lastReflectedTime: Long
    get() = properties.getLong("lastReflectedTime")
    set(value) = properties.putLong("lastReflectedTime", value)

fun ServerPlayerEntity.onDamage() {
    if (equippedSkills.none { it is ReflectionTrigger }) return
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

val PlayerEntity.skillInitialized: Boolean
    get() = try {
        Components.DATA.maybeGet(this).isPresent
    } catch (e: Exception) {
        false
    }

fun PlayerEntity.updateScreen(data: NbtCompound = NbtCompound()) {
    if (world.isClient) {
        (client!!.currentScreen as? AutoSyncedScreen)?.update(data)
    }
}

fun PlayerEntity.send(packet: Packet<out PacketListener>) {
    if (this is ServerPlayerEntity) {
        networkHandler.sendPacket(packet)
    } else if (this is ClientPlayerEntity) {
        networkHandler.sendPacket(packet)
    }
}

fun ServerPlayerEntity.spawnParticles(
    type: ParticleEffect,
    pos: Vec3d,
    count: Int,
    deltaX: Double,
    deltaY: Double,
    deltaZ: Double,
    speed: Double,
) {
    serverWorld.spawnParticles(type, pos.x, pos.y, pos.z, count, deltaX, deltaY, deltaZ, speed)
}

fun ServerPlayerEntity.playSound(sound: SoundEvent) = world.playSound(null, blockPos, sound, SoundCategory.PLAYERS)

val clientPlayer: ClientPlayerEntity?
    get() = client?.player
val PlayerEntity.horizontalRotationVector: Vec3d
    get() = getRotationVector(0f, yaw)