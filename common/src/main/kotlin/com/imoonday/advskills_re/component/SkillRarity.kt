package com.imoonday.advskills_re.component

import com.google.gson.*
import com.google.gson.JsonSerializer
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import net.minecraft.nbt.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.lang.reflect.*

class SkillRarity {

    var level: Int
        private set
    val id: String
    var roman: String
        private set
    var color: Int
        private set
    var weight: Int
        private set
    var displayName: Text
        private set
    val formatting: Formatting? get() = Formatting.entries.find { it.colorValue == color }

    constructor(
        level: Int,
        id: String,
        roman: String,
        color: Int,
        weight: Int,
        displayName: Text = translate("skillRarity.$id")
    ) {
        this.level = level
        this.id = id
        this.roman = roman
        this.color = color
        this.weight = weight
        this.displayName = displayName
    }

    constructor(
        level: Int,
        id: String,
        roman: String,
        formatting: Formatting,
        weight: Int
    ) : this(level, id, roman, formatting.colorValue ?: 0, weight)

    constructor(rarity: SkillRarity) : this(
        rarity.level,
        rarity.id,
        rarity.roman,
        rarity.color,
        rarity.weight,
        rarity.displayName
    )

    fun format(text: Text): MutableText = text.copy().run {
        formatting?.let { formatted(it) } ?: styled { it.withColor(color) }
    }

    fun toNbt(): NbtCompound = NbtCompound().apply {
        putInt("level", level)
        putString("id", id)
        putString("roman", roman)
        putInt("color", color)
        putInt("weight", weight)
        putString("displayName", Text.Serializer.toJson(displayName))
    }

    fun copyFrom(other: SkillRarity) {
        level = other.level
        roman = other.roman
        color = other.color
        weight = other.weight
        displayName = other.displayName
    }

    object SerializerById : JsonDeserializer<SkillRarity>, JsonSerializer<SkillRarity> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SkillRarity = try {
            fromIdNullable(json.asJsonPrimitive.asString) ?: run {
                LOGGER.error("Unknown Skill Rarity id: $json")
                UNKNOWN
            }
        } catch (e: Exception) {
            LOGGER.error("Error deserializing Skill Rarity: $json", e)
            UNKNOWN
        }

        override fun serialize(src: SkillRarity, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            JsonPrimitive(src.id)
    }

    object Serializer : JsonDeserializer<SkillRarity>, JsonSerializer<SkillRarity> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SkillRarity {
            try {
                val obj = json.asJsonObject
                val level = obj.get("level").asInt
                val id = obj.get("id").asString
                val roman = obj.get("roman").asString
                val color = obj.get("color").asInt
                val weight = obj.get("weight").asInt
                val displayName = context.deserialize<MutableText>(obj.get("displayName"), MutableText::class.java)
                return SkillRarity(level, id, roman, color, weight, displayName)
            } catch (e: Exception) {
                LOGGER.error("Error deserializing SkillRarity: $json", e)
                return UNKNOWN
            }
        }

        override fun serialize(src: SkillRarity, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val obj = JsonObject()
            obj.addProperty("level", src.level)
            obj.addProperty("id", src.id)
            obj.addProperty("roman", src.roman)
            obj.addProperty("color", src.color)
            obj.addProperty("weight", src.weight)
            obj.add("displayName", context.serialize(src.displayName))
            return obj
        }
    }

    companion object {

        private val LOGGER = LogUtils.getLogger()
        private val predefinedRarities = mutableMapOf<String, SkillRarity>()

        val UNKNOWN = SkillRarity(0, "unknown", "N", Formatting.GRAY, 0)
        val COMMON = create { SkillRarity(1, "common", "I", Formatting.WHITE, 8) }
        val UNCOMMON = create { SkillRarity(2, "uncommon", "II", Formatting.GREEN, 7) }
        val RARE = create { SkillRarity(3, "rare", "III", Formatting.AQUA, 6) }
        val SUPERB = create { SkillRarity(4, "superb", "IV", Formatting.GOLD, 5) }
        val EPIC = create { SkillRarity(5, "epic", "V", Formatting.RED, 4) }
        val LEGENDARY = create { SkillRarity(6, "legendary", "VI", Formatting.LIGHT_PURPLE, 3) }
        val MYTHIC = create { SkillRarity(7, "mythic", "VII", Formatting.DARK_PURPLE, 2) }
        val UNIQUE = create { SkillRarity(8, "unique", "VIII", Formatting.DARK_RED, 1) }

        private val _rarities = listOf(
            COMMON, UNCOMMON, RARE, SUPERB, EPIC, LEGENDARY, MYTHIC, UNIQUE
        ).associateBy { it.id }

        @JvmStatic
        val rarities: List<SkillRarity> = _rarities.values.toList()

        private fun create(factory: () -> SkillRarity): SkillRarity {
            val rarity = factory()
            predefinedRarities[rarity.id] = SkillRarity(rarity)
            return getOrDefault(rarity)
        }

        private fun getOrDefault(rarity: SkillRarity) =
            RarityManager.getRarity(rarity.id) ?: RarityManager.getRarity(rarity.level) ?: rarity

        @JvmStatic
        fun fromLevel(level: Int): SkillRarity = fromLevelNullable(level) ?: UNKNOWN

        @JvmStatic
        fun fromLevelNullable(level: Int): SkillRarity? =
            if (level == 0) UNKNOWN else rarities.find { it.level == level }

        @JvmStatic
        fun fromId(id: String): SkillRarity = fromIdNullable(id) ?: UNKNOWN

        @JvmStatic
        fun fromIdNullable(id: String): SkillRarity? = if (id == UNKNOWN.id) UNKNOWN else _rarities[id]

        @JvmStatic
        fun parse(string: String): SkillRarity =
            fromIdNullable(string) ?: string.toIntOrNull()?.let { fromLevel(it) } ?: UNKNOWN

        @JvmStatic
        fun fromNbt(nbt: NbtCompound): SkillRarity {
            val level = nbt.getInt("level")
            val id = nbt.getString("id")
            val roman = nbt.getString("roman")
            val color = nbt.getInt("color")
            val weight = nbt.getInt("weight")
            val displayName = Text.Serializer.fromJson(nbt.getString("displayName")) ?: translate("skillRarity.$id")
            return SkillRarity(level, id, roman, color, weight, displayName)
        }

        @JvmStatic
        fun init() = Unit

        @JvmStatic
        fun reload() {
            RarityManager.loadFiles()
            predefinedRarities.forEach { _rarities[it.key]?.copyFrom(getOrDefault(it.value)) }
            RarityManager.saveMissing(rarities)
        }

        @JvmStatic
        fun updateRarities(rarities: Collection<SkillRarity>) {
            rarities.forEach { _rarities[it.id]?.copyFrom(it) }
        }
    }
}