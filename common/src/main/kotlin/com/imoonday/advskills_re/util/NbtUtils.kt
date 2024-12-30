package com.imoonday.advskills_re.util

import net.minecraft.entity.*
import net.minecraft.nbt.*
import net.minecraft.util.math.*
import java.util.*

class NbtUtils {

    companion object {

        fun readVec3d(tag: NbtCompound?): Vec3d? {
            if (tag != null &&
                tag.contains("dx", 6) &&
                tag.contains("dy", 6) &&
                tag.contains("dz", 6)
            ) {
                return Vec3d(tag.getDouble("dx"), tag.getDouble("dy"), tag.getDouble("dz"))
            }

            return null
        }

        fun writeVec3dToTag(vec: Vec3d, tag: NbtCompound = NbtCompound()): NbtCompound {
            tag.putDouble("dx", vec.x)
            tag.putDouble("dy", vec.y)
            tag.putDouble("dz", vec.z)
            return tag
        }

        fun readEntityPositionFromTag(tag: NbtCompound?): Vec3d? {
            if (tag != null && tag.contains("Pos", NbtElement.LIST_TYPE.toInt())) {
                val tagList = tag.getList("Pos", NbtElement.DOUBLE_TYPE.toInt())

                if (tagList.heldType == NbtElement.DOUBLE_TYPE && tagList.size == 3) {
                    return Vec3d(tagList.getDouble(0), tagList.getDouble(1), tagList.getDouble(2))
                }
            }

            return null
        }

        fun writeEntityPositionToTag(pos: Vec3d, tag: NbtCompound = NbtCompound()): NbtCompound {
            val posList = NbtList()

            posList.add(NbtDouble.of(pos.x))
            posList.add(NbtDouble.of(pos.y))
            posList.add(NbtDouble.of(pos.z))
            tag.put("Pos", posList)

            return tag
        }

        fun readGlobalPosFromTag(tag: NbtCompound?): Optional<GlobalPos> {
            if (tag != null) {
                return GlobalPos.CODEC
                    .parse(NbtOps.INSTANCE, tag.get("GlobalPos"))
                    .result()
            }
            return Optional.empty()
        }

        fun writeGlobalPosToTag(globalPos: GlobalPos, tag: NbtCompound = NbtCompound()): NbtCompound {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos).result().ifPresent { tag.put("GlobalPos", it) }
            return tag
        }

        fun writeEntityGlobalPosToTag(entity: Entity, tag: NbtCompound = NbtCompound()): NbtCompound {
            val globalPos = GlobalPos.create(entity.world.registryKey, entity.blockPos)
            return writeGlobalPosToTag(globalPos, tag)
        }
    }
}

inline fun <reified T : Number> Collection<T>.toNbtNumberList(): NbtList = when (T::class) {
    Int::class -> toNbtList { NbtInt.of(it.toInt()) }
    Float::class -> toNbtList { NbtFloat.of(it.toFloat()) }
    Double::class -> toNbtList { NbtDouble.of(it.toDouble()) }
    Byte::class -> toNbtList { NbtByte.of(it.toByte()) }
    Long::class -> toNbtList { NbtLong.of(it.toLong()) }
    Short::class -> toNbtList { NbtShort.of(it.toShort()) }
    else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
}

fun Collection<Boolean>.toNbtBooleanList(): NbtList = toNbtList(NbtByte::of)

fun Collection<String>.toNbtStringList(): NbtList = toNbtList(NbtString::of)

fun Collection<ByteArray>.toNbtByteArrayList(): NbtList = toNbtList(::NbtByteArray)

fun Collection<IntArray>.toNbtIntArrayList(): NbtList = toNbtList(::NbtIntArray)

fun Collection<LongArray>.toNbtLongArrayList(): NbtList = toNbtList(::NbtLongArray)

fun Collection<UUID>.toNbtUUIDList(): NbtList = toNbtList(NbtHelper::fromUuid)

fun Collection<NbtCompound>.toNbtCompoundList(): NbtList = toNbtList { it }

fun <T> Collection<T>.toNbtList(cast: (T) -> NbtElement): NbtList = NbtList().apply {
    addAll(this@toNbtList.map { cast(it) })
}

fun <K, V> Map<K, V>.toNbtCompound(putAction: NbtCompound.(K, V) -> Unit): NbtCompound = NbtCompound().apply {
    forEach { (k, v) -> putAction(k, v) }
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
inline fun <reified T : NbtElement, R : Any> NbtList.castToList(cast: (T) -> R?): List<R> =
    mapNotNull { if (it is T) cast(it) else null }

fun <V> NbtCompound.toStringMap(getAction: NbtCompound.(String) -> V?): MutableMap<String, V> =
    LinkedHashMap<String, V>().apply {
        this@toStringMap.keys.forEach {
            val value = getAction(it)
            if (value != null) {
                put(it, value)
            }
        }
    }
