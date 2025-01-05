package com.imoonday.advskills_re.skill

import com.google.gson.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.Enhancement.Operation.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.util.*

@Suppress("LeakingThis")
abstract class Skill(settings: Settings) : SkillTrigger {

    private val defaultSettings: Settings = settings
    val settings: Settings

    val id: Identifier get() = settings.id
    val name: Text get() = settings.name
    val description: Text get() = settings.description
    val icon: Identifier get() = settings.icon
    val types: Set<SkillType> get() = settings.types
    val defaultCooldown: Int get() = settings.cooldown
    val rarity: SkillRarity get() = settings.rarity
    val weight: Int get() = settings.weight
    val invalid: Boolean
        get() = settings.invalid
            || SkillConfig.get().isInBlackList(id)
            || GlobalConfig.get().skillConfig.isInBlackList(id)
    private val parameters: Map<String, Parameter> get() = settings.parameters
    private val enhancements: List<Enhancement> get() = settings.enhancements.toList()
    val item: SkillItem? get() = Registries.ITEM[id] as? SkillItem

    val cooldown: Int
        get() {
            val cooldown = defaultCooldown
            val multiplier = SkillConfig.get().skillCooldownMultiplier
                ?: GlobalConfig.get().skillConfig.skillCooldownMultiplier
                ?: return cooldown
            return (cooldown * multiplier).toInt()
        }

    val formattedName: MutableText
        get() = rarity.format(name)

    val hoverableName: MutableText
        get() = item?.run {
            formattedName.styled {
                it.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        HoverEvent.ItemStackContent(defaultStack)
                    )
                )
            }
        } ?: formattedName

    val cooldownText: MutableText get() = getCooldownText(cooldown)
    val defaultCooldownText: MutableText get() = getCooldownText(defaultCooldown)
    private val enhancementDescArgs: MutableMap<String, ((value: Double) -> Any)?> = mutableMapOf()

    init {
        if (this.defaultSettings.cooldown > 0) {
            this.defaultSettings.addEnhancement(
                id = "cooldown",
                value = -0.16,
                operation = MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
        }
        initDefaultSettings(this.defaultSettings)
        this.settings = Settings(this.defaultSettings)
    }

    open fun updateSettings(settings: Settings) {
        if (settings.id == this.settings.id) {
            this.settings.copyFrom(settings)
        }
    }

    abstract fun initDefaultSettings(settings: Settings)

    fun isEmpty(): Boolean = this === Skills.EMPTY || this is EmptySkill

    fun createUuid(content: String): UUID = UUID.nameUUIDFromBytes("$id-$content".toByteArray())

    open fun getItemTooltips(displayName: Boolean = false, displayId: Boolean = false): MutableList<Text> =
        mutableListOf<Text>().apply {
            if (displayName) {
                add(formattedName)
            }
            add(description.copy().formatted(Formatting.GRAY))
            add(Text.empty())
            add(translate(
                "screen.gallery.info.type",
                types.joinToString(" ") { it.displayName.string }
            ).styled { it.withColor(0x4FC3F7) })
            add(translate("screen.gallery.info.cooldown", cooldownText).styled { it.withColor(0x81C784) })
            add(translate("screen.gallery.info.rarity", rarity.displayName).let { rarity.format(it) })
            if (displayId) {
                add(id.toString().toText().formatted(Formatting.DARK_GRAY))
            }
        }

    protected fun addParameter(
        name: String,
        baseValue: Number,
        enhancementId: String,
        value: Number,
        operation: Enhancement.Operation,
        maxLevel: Int,
        descArg: (value: Double) -> Any = Enhancement.ArgFormatter.FLOAT
    ) {
        val formatter = descArg as? Enhancement.ArgFormatter
        this.settings.addParameter(
            name, baseValue, enhancementId, value.toDouble(), operation, maxLevel, formatter, false
        )
        if (formatter == null) {
            this.addEnhancementDescArg(enhancementId, descArg)
        }
    }

    protected fun addEnhancement(
        id: String,
        value: Number,
        operation: Enhancement.Operation,
        maxLevel: Int,
        descArg: (value: Double) -> Any = Enhancement.ArgFormatter.FLOAT
    ) {
        val formatter = descArg as? Enhancement.ArgFormatter
        this.settings.addEnhancement(
            id, value.toDouble(), operation, maxLevel, formatter, false
        )
        if (formatter == null) {
            this.addEnhancementDescArg(id, descArg)
        }
    }

    fun addEnhancementDescArg(id: String, arg: (value: Double) -> Any) {
        this.enhancementDescArgs[id] = arg
    }

    fun getParam(name: String): Parameter? = parameters[name]

    fun getAvailableEnhancements(): List<Enhancement> = enhancements.filter { it.maxLevel > 0 }

    fun getEnhancement(id: String): Enhancement? = enhancements.firstOrNull { it.id == id }

    fun hasEnhancement(id: String): Boolean = enhancements.any { it.id == id }

    abstract fun use(user: ServerPlayerEntity): UseResult

    open fun PlayerEntity.playSoundFromParam(name: String, default: SoundEvent?, exceptSelf: Boolean = false) {
        getSoundEventParam(name, default)?.let {
            world.playSound(
                if (exceptSelf) this else null,
                blockPos,
                it,
                SoundCategory.PLAYERS,
                random.nextFloat() * 0.2f + 0.9f,
                random.nextFloat() * 0.2f + 0.9f
            )
        }
    }

    fun tryUse(player: ServerPlayerEntity, keyState: UseSkillC2SRequest.KeyState) {
        if (invalid) return
        if ((this !is LongPressTrigger || !player.isUsing()) && keyState == UseSkillC2SRequest.KeyState.RELEASE) return
        if (this is LongPressTrigger && player.isUsing() && keyState == UseSkillC2SRequest.KeyState.PRESS) return
        if (player.isSilenced) {
            player.sendMessage(translate("useSkill.silenced"), true)
            return
        }
        if (player.isCooling() && (this !is LongPressTrigger || !player.isUsing())) {
            player.sendMessage(
                translate(
                    "useSkill.cooling",
                    name,
                    translate("cooldown.seconds", player.getCooldown(this) / 20.0)
                ), true
            )
        } else {
            player.forEachTrigger<UseInterruptTrigger>({ it != this && it.shouldInterrupt(player) }) {
                it.interrupt(
                    player
                )
            }
            val result = (this as? LongPressTrigger)?.use(player, keyState) ?: use(player)
            handleResult(player, result)
        }
    }

    fun handleResult(player: ServerPlayerEntity, result: UseResult) {
        result.sound?.let { player.playSound(it) }
        if (result.success) {
            player.sendMessage(result.message ?: this.name, true)
        } else {
            val message = result.message ?: translate("useSkill.failed", name)
            if (message.content != TextContent.EMPTY) {
                player.sendMessage(message, true)
            }
        }
        if (result.cooling) {
            player.startCooling()
            if (this is SynchronousCoolingTrigger) {
                this.getOtherSkills(player).forEach(player::startCooling)
            }
        }
    }

    override fun getAsSkill(): Skill = this

    fun failedMessage() = translateSkill(id.path, "failed")

    fun message(key: String, vararg args: Any) = translateSkill(id.path, key, *args)

    fun messageKey(key: String) = translateSkillKey(id.path, key)

    open fun applyCooldownEnhancements(player: PlayerEntity, cooldown: Int): Int {
        val (enhancement, data) = player.getEnhancement("cooldown") ?: return cooldown
        return enhancement.getEnhancedValue(data.currentLevel, cooldown).coerceAtLeast(0)
    }

    open fun getEnhancementTooltip(id: String, level: Int): Text? =
        getEnhancement(id)?.let {
            val arg = (it.descArg ?: enhancementDescArgs[it.id])?.invoke(it.getValue(level))
            if (arg != null) Text.translatable(it.description, arg)
            else Text.translatable(it.description)
        }

    open fun getEnhancementTooltips(player: PlayerEntity): List<Text> =
        player.getEnhancements().map {
            val enhancement = it.key
            val level = it.value.currentLevel
            val arg = (enhancement.descArg ?: enhancementDescArgs[enhancement.id])?.invoke(enhancement.getValue(level))
            (if (arg != null) Text.translatable(enhancement.description, arg)
            else Text.translatable(enhancement.description)).formatted(Formatting.BLUE)
        }

    fun getCooldownText(cooldown: Int) = if (cooldown <= 0) {
        translate("cooldown.none")
    } else {
        val text = (cooldown / 20.0).toString()
        translate(
            "cooldown.seconds",
            if (text.endsWith(".0")) text.substring(0, text.length - 2) else text
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Skill) return false

        return this.id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "Skill(id=$id, cooldown=$cooldown, icon=$icon, invalid=$invalid, parameters=$parameters, rarity=$rarity, types=$types, weight=$weight, enhancements=$enhancements)"

    data class Settings(
        val id: Identifier,
        var name: Text,
        var description: Text,
        var icon: Identifier = id("unknown.png"),
        var types: Set<SkillType> = emptySet(),
        var cooldown: Int = 0,
        var rarity: SkillRarity,
        var invalid: Boolean = false,
        var weight: Int = rarity.weight,
        var drawable: Boolean = true,
        val parameters: MutableMap<String, Parameter> = mutableMapOf(),
        val enhancements: MutableList<Enhancement> = mutableListOf(),
    ) {

        constructor(
            id: String,
            types: Collection<SkillType> = emptyList(),
            cooldown: Int = 0,
            rarity: SkillRarity
        ) : this(
            id = id(id),
            name = translateSkill(id, "name"),
            description = translateSkill(id, "description"),
            icon = itemId(id),
            types = types.toSet(),
            cooldown = cooldown * 20,
            rarity = rarity
        )

        constructor(settings: Settings) : this(
            id = settings.id,
            name = settings.name,
            description = settings.description,
            icon = settings.icon,
            types = settings.types.toSet(),
            cooldown = settings.cooldown,
            rarity = settings.rarity,
            invalid = settings.invalid,
            weight = settings.weight,
            drawable = settings.drawable,
            parameters = settings.parameters.toMutableMap(),
            enhancements = settings.enhancements.toMutableList()
        )

        fun withName(name: Text): Settings {
            this.name = name
            return this
        }

        fun withDescription(description: Text): Settings {
            this.description = description
            return this
        }

        fun withIcon(icon: Identifier): Settings {
            this.icon = icon
            return this
        }

        fun withTypes(types: Collection<SkillType>): Settings {
            this.types = LinkedHashSet(types)
            return this
        }

        fun addType(type: SkillType): Settings {
            this.types += type
            return this
        }

        fun addTypeToTop(type: SkillType): Settings {
            this.types = linkedSetOf(type) + this.types
            return this
        }

        fun withCooldown(cooldown: Int): Settings {
            this.cooldown = cooldown
            return this
        }

        fun withRarity(rarity: SkillRarity): Settings {
            this.rarity = rarity
            return this
        }

        fun withInvalid(invalid: Boolean): Settings {
            this.invalid = invalid
            return this
        }

        fun withWeight(weight: Int): Settings {
            this.weight = weight
            return this
        }

        fun withDrawable(drawable: Boolean): Settings {
            this.drawable = drawable
            return this
        }

        fun withParameters(parameters: Map<String, Parameter>): Settings {
            this.parameters.clear()
            this.parameters.putAll(parameters)
            return this
        }

        fun withEnhancements(enhancements: Collection<Enhancement>): Settings {
            this.enhancements.clear()
            this.enhancements.addAll(enhancements)
            return this
        }

        fun addParameter(name: String, baseValue: Any, vararg enhancementIds: String): Settings {
            parameters[name] = Parameter.create(baseValue, enhancementIds.toList())
            return this
        }

        fun addParameter(
            name: String,
            baseValue: Number,
            enhancementId: String,
            value: Number,
            operation: Enhancement.Operation,
            maxLevel: Int,
            descArg: Enhancement.ArgFormatter?,
            genericText: Boolean = false
        ): Settings {
            parameters[name] = Parameter.create(baseValue, listOf(enhancementId))
            return addEnhancement(enhancementId, value.toDouble(), operation, maxLevel, descArg, genericText)
        }

        fun addParameter(
            name: String,
            baseValue: Boolean,
            enhancementId: String,
            genericText: Boolean = false
        ): Settings {
            parameters[name] = Parameter.create(baseValue, listOf(enhancementId))
            return addEnhancement(enhancementId, genericText)
        }

        fun addEnhancement(
            id: String,
            value: Number,
            operation: Enhancement.Operation,
            maxLevel: Int,
            descArg: Enhancement.ArgFormatter?,
            genericText: Boolean = false
        ): Settings {
            val name = createName(genericText, id)
            val descriptionKey = createDescriptionKey(genericText, id)
            val enhancement = when (operation) {
                ADDITION -> Enhancement.Increment(
                    id = id,
                    name = name,
                    description = descriptionKey,
                    valuePerLvl = value.toDouble(),
                    maxLevel = maxLevel,
                    weight = Enhancement.Weight.Incremental(maxLevel * 2, -2),
                    descArg = descArg
                )

                MULTIPLY_BASE -> Enhancement.MultiplyBase(
                    id = id,
                    name = name,
                    description = descriptionKey,
                    valuePerLvl = value.toDouble(),
                    maxLevel = maxLevel,
                    weight = Enhancement.Weight.Incremental(maxLevel * 2, -2),
                    descArg = descArg
                )

                MULTIPLY_TOTAL -> Enhancement.MultiplyTotal(
                    id = id,
                    name = name,
                    description = descriptionKey,
                    valuePerLvl = value.toDouble(),
                    maxLevel = maxLevel,
                    weight = Enhancement.Weight.Incremental(maxLevel * 2, -2),
                    descArg = descArg
                )

                NONE -> return addEnhancement(id)
            }
            enhancements += enhancement
            return this
        }

        fun addEnhancement(id: String, genericText: Boolean = false): Settings {
            val enhancement =
                Enhancement.LevelLess(
                    id = id,
                    name = createName(genericText, id),
                    description = createDescriptionKey(genericText, id),
                    weight = Enhancement.Weight.Fixed(5)
                )
            enhancements += enhancement
            return this
        }

        private fun createDescriptionKey(genericText: Boolean, id: String) =
            if (genericText) translateKey("enhancement.$id.description")
            else translateSkillKey(this@Settings.id.path, "$id.description")

        private fun createName(genericText: Boolean, id: String) =
            if (genericText) translate("enhancement.$id.name")
            else translateSkill(this@Settings.id.path, "$id.name")

        fun copyFrom(other: Settings) {
            if (this.id != other.id) {
                LOGGER.warn(
                    "${this.id} is copying settings from different id: ${other.id}, please make sure the parameters are correct"
                )
            }
            this.name = other.name
            this.description = other.description
            this.icon = other.icon
            this.types = other.types.toSet()
            this.cooldown = other.cooldown
            this.rarity = other.rarity
            this.invalid = other.invalid
            this.weight = other.weight
            this.drawable = other.drawable
            this.parameters.clear()
            this.parameters.putAll(other.parameters)
            this.enhancements.clear()
            this.enhancements.addAll(other.enhancements)
        }

        fun toNbt(): NbtCompound = NbtCompound().apply {
            putString("id", id.toString())
            putString("name", Text.Serializer.toJson(name))
            putString("description", Text.Serializer.toJson(description))
            putString("icon", icon.toString())
            putIntArray("types", types.map { it.ordinal })
            putInt("cooldown", cooldown)
            putString("rarity", rarity.id)
            putBoolean("invalid", invalid)
            putInt("weight", weight)
            putBoolean("drawable", drawable)
            put("parameters", parameters.toNbtCompound { k, v -> put(k, v.toNbt()) })
            put("enhancements", enhancements.toNbtList { it.toNbt() })
        }

        fun toJson(): String = GSON.toJson(this)

        fun toJsonTree(): JsonElement = GSON.toJsonTree(this)

        companion object {

            private val LOGGER = LogUtils.getLogger()

            @JvmStatic
            val GSON: Gson = GsonBuilder().setPrettyPrinting().setLenient()
                .registerTypeAdapter(Identifier::class.java, Identifier.Serializer())
                .registerTypeAdapter(SoundEvent::class.java, Serializers.SOUND_EVENT)
                .registerTypeHierarchyAdapter(Text::class.java, Serializers.TextSerializer)
                .registerTypeHierarchyAdapter(Enhancement::class.java, Enhancement.Serializer)
                .registerTypeHierarchyAdapter(Parameter::class.java, Parameter.Serializer)
                .registerTypeAdapter(SkillRarity::class.java, SkillRarity.SerializerById)
                .registerTypeHierarchyAdapter(Enhancement.Weight::class.java, Enhancement.Weight.Serializer)
                .registerTypeAdapter(
                    Parameter.SoundEventParameter.soundType,
                    Serializers.OptionalSerializer(SoundEvent::class.java)
                )
                .registerTypeHierarchyAdapter(
                    Parameter.ListParameter.PrimitiveType::class.java,
                    Parameter.ListParameter.PrimitiveType.Serializer
                )
                .create()

            @JvmStatic
            fun fromNbt(nbt: NbtCompound): Settings? {
                val id = nbt.getString("id").toIdentifier() ?: return null
                val name = Text.Serializer.fromJson(nbt.getString("name")) ?: translate("enhancement.$id.name")
                val description =
                    Text.Serializer.fromJson(nbt.getString("description")) ?: translate("enhancement.$id.description")
                val icon = nbt.getString("icon").toIdentifier() ?: id("unknown.png")
                val types = nbt.getIntArray("types").map { SkillType.entries.getOrNull(it) }.filterNotNull().toSet()
                val cooldown = nbt.getInt("cooldown")
                val rarity = SkillRarity.fromId(nbt.getString("rarity"))
                val invalid = nbt.getBoolean("invalid")
                val weight = nbt.getInt("weight")
                val drawable = nbt.getBoolean("drawable")
                val parameters = nbt.getCompound("parameters").toStringMap { Parameter.fromNbt(getCompound(it)) }
                val enhancements = nbt.getList("enhancements", NbtElement.COMPOUND_TYPE.toInt())
                    .mapNotNull { element -> (element as? NbtCompound)?.let { Enhancement.fromNbt(it) } }
                return Settings(
                    id, name, description, icon, types, cooldown, rarity,
                    invalid, weight, drawable,
                    parameters, enhancements.toMutableList()
                )
            }

            @JvmStatic
            fun fromJson(json: String): Settings? = try {
                GSON.fromJson(json, Settings::class.java)
            } catch (e: Exception) {
                LOGGER.error("Failed to parse skill settings from json: $json", e)
                null
            }
        }
    }
}