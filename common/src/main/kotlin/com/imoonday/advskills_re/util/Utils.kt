package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.*
import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.mixin.*
import dev.architectury.event.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.util.math.*
import java.awt.*
import java.nio.file.*
import kotlin.io.path.*
import kotlin.math.*
import kotlin.random.*

object Utils {

    @JvmStatic
    fun levenshteinDistance(str1: String, str2: String): Int {
        val lenStr1 = str1.length
        val lenStr2 = str2.length
        val dp = Array(lenStr1 + 1) { IntArray(lenStr2 + 1) }
        for (i in 0..lenStr1) {
            for (j in 0..lenStr2) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = min(
                        dp[i - 1][j - 1] + if (str1[i - 1] == str2[j - 1]) 0 else 1,
                        min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    )
                }
            }
        }
        return dp[lenStr1][lenStr2]
    }

    @JvmStatic
    fun similarityScore(str1: String, str2: String): Double {
        val maxLen = maxOf(str1.length, str2.length)
        return 1.0 - levenshteinDistance(str1, str2).toDouble() / maxLen
    }

    @JvmStatic
    fun <T> findMostSimilarElement(target: String, candidates: Collection<T>, formatter: (T) -> String): T? =
        candidates.maxByOrNull { similarityScore(formatter(it), target) }
}

fun Color.alpha(alpha: Int): Color = Color(this.red, this.green, this.blue, alpha)

fun Color.alpha(multiplier: Double): Color = alpha((this.alpha * multiplier).toInt())

fun StatusEffectInstance.setDuration(duration: Int) {
    (this as StatusEffectInstanceAccessor).setDuration(duration)
}

val Box.blockPosSet: Set<BlockPos>
    get() {
        val set = mutableSetOf<BlockPos>()
        for (x in floor(minX).toInt()..floor(maxX).toInt()) {
            for (z in floor(minZ).toInt()..floor(maxZ).toInt()) {
                for (y in floor(minY).toInt()..floor(maxY).toInt()) {
                    set.add(BlockPos(x, y, z))
                }
            }
        }
        return set
    }

fun id(name: String): Identifier = Identifier(MOD_ID, name)

fun itemId(name: String): Identifier = id("textures/item/$name.png")

val Box.volume: Double
    get() = this.xLength * this.yLength * this.zLength

fun Vec3d.toBlockPos(): BlockPos = BlockPos.ofFloored(this)

fun String.toText(): MutableText = Text.literal(this)

fun String.toIdentifier() = Identifier.tryParse(this)

fun Boolean?.toEventResult(): EventResult = EventResult.interrupt(this)

val Entity.horizontalRotationVector: Vec3d
    get() = getRotationVector(0f, yaw)

fun <T> Collection<T>.randomByWeight(
    weightMapper: (T) -> Int,
    defaultValue: T
): T {
    if (isEmpty()) return defaultValue

    val totalWeight = sumOf { weightMapper(it) }

    if (totalWeight <= 0) return defaultValue

    val randomValue = Random.nextInt(totalWeight)

    var currentWeight = 0
    for (item in this) {
        currentWeight += weightMapper(item)
        if (randomValue < currentWeight) {
            return item
        }
    }

    return defaultValue
}

fun <T> Collection<T>.randomByWeight(
    weightMapper: (T) -> Int,
    count: Int,
    defaultValue: T
): List<T> {
    if (count <= 0) return emptyList()
    if (isEmpty()) return List(count) { defaultValue }

    val totalWeight = sumOf { weightMapper(it) }

    if (totalWeight <= 0) return List(count) { defaultValue }

    val weightedItems = map { it to weightMapper(it) }

    val selectedItems = mutableListOf<T>()

    while (selectedItems.size < count && selectedItems.size < size) {
        val randomValue = Random.nextInt(totalWeight)
        var currentWeight = 0

        for ((item, weight) in weightedItems) {
            currentWeight += weight
            if (randomValue < currentWeight && item !in selectedItems) {
                selectedItems.add(item)
                break
            }
        }
    }

    while (selectedItems.size < count) {
        selectedItems.add(defaultValue)
    }

    return selectedItems.toList()
}

fun <T> Collection<T>.toText(
    formatter: (T) -> MutableText?,
    separator: Text = ", ".toText(),
    prefix: Text = Text.empty(),
    suffix: Text = Text.empty(),
): MutableText {
    var text = prefix.copy()
    if (isEmpty()) return text

    forEachIndexed { i, item ->
        val formattedText = formatter(item)
        if (formattedText != null) {
            text = text.append(formattedText)
            if (i != size - 1) {
                text = text.append(separator)
            }
        } else if (i != size - 1) {
            text.siblings.removeLast()
        }
    }

    return text.append(suffix)
}

fun NbtCompound.clear() = keys.toSet().forEach(::remove)

fun NbtCompound.replaceAll(nbt: NbtCompound?) {
    clear()
    nbt?.let { copyFrom(it) }
}

val Entity.centerPos: Vec3d
    get() = Vec3d(pos.x, pos.y + height / 2, pos.z)

val Entity.wasHorizontalCollision: Boolean
    get() = (this as ICollisionRecorder).wasHorizontalCollision()

val Entity.wasVerticalCollision: Boolean
    get() = (this as ICollisionRecorder).wasVerticalCollision()

val Entity.wasGroundCollision: Boolean
    get() = (this as ICollisionRecorder).wasGroundCollision()

fun Entity.hasMoved(): Boolean = prevX != x || prevY != y || prevZ != z

inline fun <reified T : Number> Number.toNumber(): T {
    return when (T::class) {
        Byte::class -> this.toByte() as T
        Short::class -> this.toShort() as T
        Int::class -> this.toInt() as T
        Long::class -> this.toLong() as T
        Float::class -> this.toFloat() as T
        Double::class -> this.toDouble() as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

fun ServerWorld.addTask(interval: Int, repeat: Int, task: () -> Boolean) {
    if (repeat <= 0) return
    (this as TaskHandler).addTask(LoopTask(interval, repeat, task))
}

fun ServerWorld.executeAndAddTask(interval: Int, repeat: Int, task: () -> Boolean) {
    if (repeat <= 0) return

    task()
    addTask(interval, repeat - 1, task)
}

fun ServerPlayerEntity.addTask(interval: Int, repeat: Int, task: () -> Boolean) =
    serverWorld.addTask(interval, repeat, task)

fun ServerPlayerEntity.executeAndAddTask(interval: Int, repeat: Int, task: () -> Boolean) =
    serverWorld.executeAndAddTask(interval, repeat, task)

fun Path.listAllFiles(glob: String = "*"): List<Path> = mutableListOf<Path>().apply {
    listDirectoryEntries().forEach {
        if (it.isDirectory()) {
            addAll(it.listAllFiles(glob))
        } else if (glob == "*" || it.name.matches(glob.toRegex())) {
            add(it)
        }
    }
}