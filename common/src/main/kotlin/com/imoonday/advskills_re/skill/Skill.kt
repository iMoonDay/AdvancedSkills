package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.Enhancement.Operation.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.enhancement.*
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
import java.util.function.*

abstract class Skill(val settings: Settings) : SkillTrigger {

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
    private val parameters: Map<String, SkillParameter> get() = settings.parameters
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
    private val enhancementDescArgs: MutableMap<String, ((value: Float) -> Any)?> = mutableMapOf()

    init {
        if (defaultCooldown > 0) {
            addEnhancement(
                id = "cooldown",
                value = -0.16f,
                operation = MULTIPLY,
                maxLevel = 5,
                genericText = true,
                descArg = Enhancement.ArgFormatters.INT_PERCENT
            )
        }
    }

    @Deprecated("Use Settings instead")
    internal constructor(
        id: String,
        types: List<SkillType>,
        cooldown: Int = 0,
        rarity: SkillRarity,
        sound: Supplier<SoundEvent>? = null,
        enhancements: Set<SkillEnhancementType<*>> = emptySet(),
    ) : this(Settings(id, types, cooldown, rarity))

    fun updateSettings(settings: Settings) {
        if (settings.id == this.settings.id) {
            this.settings.copyFrom(settings)
        }
    }

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

    protected fun addEnhanceableParameter(
        name: String,
        baseValue: Number,
        enhancementId: String,
        value: Float,
        operation: Enhancement.Operation,
        maxLevel: Int,
        genericText: Boolean = false,
        descArg: (value: Float) -> Any = Enhancement.ArgFormatters.SELF
    ) {
        this.settings.addEnhanceableParameter(name, baseValue, enhancementId, value, operation, maxLevel, genericText)
        this.addEnhancementDescArg(enhancementId, descArg)
    }

    protected fun addEnhancement(
        id: String,
        value: Float,
        operation: Enhancement.Operation,
        maxLevel: Int,
        genericText: Boolean = false,
        descArg: (value: Float) -> Any = Enhancement.ArgFormatters.SELF
    ) {
        this.settings.addEnhancement(id, value, operation, maxLevel, genericText)
        this.addEnhancementDescArg(id, descArg)
    }

    protected fun addEnhancementDescArg(id: String, arg: (value: Float) -> Any) {
        this.enhancementDescArgs[id] = arg
    }

    fun getParam(name: String): SkillParameter? = parameters[name]

    fun getAvailableEnhancements(): List<Enhancement> = enhancements.filter { it.maxLevel > 0 }

    fun getEnhancement(id: String): Enhancement? = enhancements.firstOrNull { it.id == id }

    fun hasEnhancement(id: String): Boolean = enhancements.any { it.id == id }

    @Deprecated("Use getEnhancement instead")
    protected fun <T : SkillEnhancement> addEnhancementTooltipWithArg(type: SkillEnhancementType<T>, arg: (T) -> Any) {
    }

    abstract fun use(user: ServerPlayerEntity): UseResult

    open fun PlayerEntity.playSoundFromParam(name: String, exceptSelf: Boolean = false) {
        getSoundEventParam(name)?.let {
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

    open fun isDangerous(player: ServerPlayerEntity): Boolean = false

    fun failedMessage() = translateSkill(id.path, "failed")

    fun message(key: String, vararg args: Any) = translateSkill(id.path, key, *args)

    fun messageKey(key: String) = translateSkillKey(id.path, key)

    open fun applyCooldownEnhancements(player: PlayerEntity, cooldown: Int): Int {
        val (enhancement, level) = player.getEnhancement("cooldown_reduction") ?: return cooldown
        return enhancement.getEnhancedValue(level, cooldown).coerceAtLeast(0)
    }

    open fun getEnhancementTooltips(player: PlayerEntity): List<Text> =
        player.getEnhancements().map {
            val enhancement = it.key
            val level = it.value
            val arg = enhancementDescArgs[enhancement.id]?.invoke(enhancement.getValue(level))
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
        val parameters: MutableMap<String, SkillParameter> = mutableMapOf(),
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

        fun setName(name: Text): Settings {
            this.name = name
            return this
        }

        fun setDescription(description: Text): Settings {
            this.description = description
            return this
        }

        fun setIcon(icon: Identifier): Settings {
            this.icon = icon
            return this
        }

        fun setTypes(types: Collection<SkillType>): Settings {
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

        fun setCooldown(cooldown: Int): Settings {
            this.cooldown = cooldown
            return this
        }

        fun setRarity(rarity: SkillRarity): Settings {
            this.rarity = rarity
            return this
        }

        fun setInvalid(invalid: Boolean): Settings {
            this.invalid = invalid
            return this
        }

        fun setWeight(weight: Int): Settings {
            this.weight = weight
            return this
        }

        fun setParameters(parameters: Map<String, SkillParameter>): Settings {
            this.parameters.clear()
            this.parameters.putAll(parameters)
            return this
        }

        fun setEnhancements(enhancements: Collection<Enhancement>): Settings {
            this.enhancements.clear()
            this.enhancements.addAll(enhancements)
            return this
        }

        fun addParameter(name: String, baseValue: Any): Settings {
            parameters[name] = SkillParameter.create(baseValue)
            return this
        }

        fun addEnhanceableParameter(
            name: String,
            baseValue: Number,
            enhancementId: String,
            value: Float,
            operation: Enhancement.Operation,
            maxLevel: Int,
            genericText: Boolean = false
        ): Settings {
            parameters[name] = SkillParameter.create(baseValue, listOf(enhancementId))
            return addEnhancement(enhancementId, value, operation, maxLevel, genericText)
        }

        fun addEnhancement(
            id: String,
            value: Float,
            operation: Enhancement.Operation,
            maxLevel: Int,
            genericText: Boolean = false
        ): Settings {
            val name = createName(genericText, id)
            val descriptionKey = createDescriptionKey(genericText, id)
            val enhancement = when (operation) {
                ADDITION -> Enhancement.Increment(id, name, descriptionKey, value, maxLevel)
                MULTIPLY -> Enhancement.Multiply(id, name, descriptionKey, value, maxLevel)
                NONE -> return addEnhancement(id)
            }
            enhancements += enhancement
            return this
        }

        fun addEnhancement(id: String, genericText: Boolean = false): Settings {
            val enhancement =
                Enhancement.LevelLess(id, createName(genericText, id), createDescriptionKey(genericText, id))
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
                LOGGER.warn("${this.id} is copying settings from different id: ${other.id}, please make sure the parameters are correct")
            }
            this.name = other.name
            this.description = other.description
            this.icon = other.icon
            this.types = other.types.toSet()
            this.cooldown = other.cooldown
            this.rarity = other.rarity
            this.invalid = other.invalid
            this.weight = other.weight
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
            put("parameters", parameters.toNbtCompound { k, v -> put(k, v.toNbt()) })
            put("enhancements", enhancements.toNbtList { it.toNbt() })
        }

        companion object {

            private val LOGGER = LogUtils.getLogger()

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
                val parameters = nbt.getCompound("parameters").toStringMap { SkillParameter.fromNbt(getCompound(it)) }
                val enhancements = nbt.getList("enhancements", NbtElement.COMPOUND_TYPE.toInt())
                    .mapNotNull { element -> (element as? NbtCompound)?.let { Enhancement.fromNbt(it) } }
                return Settings(
                    id, name, description, icon, types, cooldown, rarity,
                    invalid, weight, parameters, enhancements.toMutableList()
                )
            }
        }
    }
}