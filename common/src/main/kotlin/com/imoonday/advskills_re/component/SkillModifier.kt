package com.imoonday.advskills_re.component

import com.imoonday.advskills_re.skill.enums.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.minecraft.nbt.*

@Serializable(with = SkillModifier.Serializer::class)
data class SkillModifier(
    var cooldown: Int?,
    var rarity: SkillRarity?,
    var time: Int?,
) {

    val isEmpty: Boolean
        get() = cooldown == null && rarity == null && time == null

    fun toNbt(): NbtCompound = NbtCompound().apply {
        if (cooldown != null) {
            putInt("cooldown", cooldown!!)
        }
        if (rarity != null) {
            putInt("rarity", rarity!!.ordinal)
        }
        if (time != null) {
            putInt("time", time!!)
        }
    }

    class Serializer : KSerializer<SkillModifier> {

        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("SkillModifier") {
                element<Int?>("cooldown")
                element<Int?>("rarity")
                element<Int?>("time")
            }

        override fun deserialize(decoder: Decoder): SkillModifier =
            decoder.decodeStructure(descriptor) {
                var cooldown: Int? = null
                var rarity: SkillRarity? = null
                var time: Int? = null
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> cooldown = decodeIntElement(descriptor, 0)
                        1 -> rarity = SkillRarity.fromId(decodeStringElement(descriptor, 1))
                        2 -> time = decodeIntElement(descriptor, 2)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
                SkillModifier(cooldown, rarity, time)
            }

        override fun serialize(encoder: Encoder, value: SkillModifier) =
            encoder.encodeStructure(descriptor) {
                if (value.cooldown != null) {
                    encodeIntElement(descriptor, 0, value.cooldown!!)
                }
                if (value.rarity != null) {
                    encodeStringElement(descriptor, 1, value.rarity!!.id)
                }
                if (value.time != null) {
                    encodeIntElement(descriptor, 2, value.time!!)
                }
            }
    }

    companion object {

        val EMPTY = SkillModifier(null, null, null)

        fun fromNbt(nbt: NbtCompound): SkillModifier {
            val cooldown = if (nbt.contains("cooldown")) nbt.getInt("cooldown") else null
            val rarity = if (nbt.contains("rarity")) SkillRarity.entries[nbt.getInt("rarity")] else null
            val time = if (nbt.contains("time")) nbt.getInt("time") else null
            return SkillModifier(cooldown, rarity, time)
        }
    }
}
