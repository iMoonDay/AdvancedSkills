package com.imoonday.util

import net.minecraft.nbt.*
import java.util.*

inline fun <reified T : Number> List<T>.toNbtNumberList(): NbtList = NbtList().apply {
    when (T::class) {
        Int::class -> addAll(this@toNbtNumberList.map { NbtInt.of(it.toInt()) })
        Float::class -> addAll(this@toNbtNumberList.map { NbtFloat.of(it.toFloat()) })
        Double::class -> addAll(this@toNbtNumberList.map { NbtDouble.of(it.toDouble()) })
        Byte::class -> addAll(this@toNbtNumberList.map { NbtByte.of(it.toByte()) })
        Long::class -> addAll(this@toNbtNumberList.map { NbtLong.of(it.toLong()) })
        Short::class -> addAll(this@toNbtNumberList.map { NbtShort.of(it.toShort()) })
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

fun List<Boolean>.toNbtBooleanList(): NbtList = NbtList().apply {
    addAll(this@toNbtBooleanList.map { NbtByte.of(it) })
}

fun List<String>.toNbtStringList(): NbtList = NbtList().apply {
    addAll(this@toNbtStringList.map { NbtString.of(it) })
}

fun List<ByteArray>.toNbtByteArrayList(): NbtList = NbtList().apply {
    addAll(this@toNbtByteArrayList.map { NbtByteArray(it) })
}

fun List<IntArray>.toNbtIntArrayList(): NbtList = NbtList().apply {
    addAll(this@toNbtIntArrayList.map { NbtIntArray(it) })
}

fun List<LongArray>.toNbtLongArrayList(): NbtList = NbtList().apply {
    addAll(this@toNbtLongArrayList.map { NbtLongArray(it) })
}

fun List<UUID>.toNbtUUIDList(): NbtList = NbtList().apply {
    addAll(this@toNbtUUIDList.map { NbtHelper.fromUuid(it) })
}

fun List<NbtCompound>.toNbtCompoundList(): NbtList = NbtList().apply {
    addAll(this@toNbtCompoundList.map { it })
}

fun NbtList.toIntList(): List<Int> = mapNotNull { (it as? NbtInt)?.intValue() }
fun NbtList.toLongList(): List<Long> = mapNotNull { (it as? NbtLong)?.longValue() }
fun NbtList.toFloatList(): List<Float> = mapNotNull { (it as? NbtFloat)?.floatValue() }
fun NbtList.toDoubleList(): List<Double> = mapNotNull { (it as? NbtDouble)?.doubleValue() }
fun NbtList.toByteList(): List<Byte> = mapNotNull { (it as? NbtByte)?.byteValue() }
fun NbtList.toShortList(): List<Short> = mapNotNull { (it as? NbtShort)?.shortValue() }
fun NbtList.toBooleanList(): List<Boolean> = mapNotNull { (it as? NbtByte)?.byteValue() != 0.toByte() }
fun NbtList.toStringList(): List<String> = mapNotNull { (it as? NbtString)?.asString() }
fun NbtList.toByteArrayList(): List<ByteArray> = mapNotNull { (it as? NbtByteArray)?.byteArray }
fun NbtList.toIntArrayList(): List<IntArray> = mapNotNull { (it as? NbtIntArray)?.intArray }
fun NbtList.toLongArrayList(): List<LongArray> = mapNotNull { (it as? NbtLongArray)?.longArray }
fun NbtList.toUUIDList(): List<UUID> = mapNotNull { NbtHelper.toUuid(it) }
fun NbtList.toCompoundList(): List<NbtCompound> = mapNotNull { it as? NbtCompound }
