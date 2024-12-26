package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.component.Enhancement.Type.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.enhancement.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import kotlinx.serialization.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.util.*
import java.util.function.*

abstract class Skill(settings: Settings) : SkillTrigger {

    val id: Identifier = settings.id
    val name: Text = settings.name
    val description: Text = settings.description
    val icon: Identifier = settings.icon
    val types: Set<SkillType> = settings.types
    val defaultCooldown: Int = settings.cooldown
    val sound: Supplier<SoundEvent>? = null

    val invalid: Boolean = settings.invalid
        get() = field || SkillConfig.get().isInBlackList(id) || GlobalConfig.get().skillConfig.isInBlackList(id)

    val rarity: SkillRarity = settings.rarity
    val weight: Int = settings.weight

    val formattedName: MutableText
        get() = name.copy().formatted(rarity.formatting)

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

    val item: SkillItem?
        get() = Registries.ITEM[id] as? SkillItem

    val cooldown: Int
        get() {
            val cooldown = defaultCooldown
            val multiplier = SkillConfig.get().skillCooldownMultiplier
                ?: GlobalConfig.get().skillConfig.skillCooldownMultiplier
                ?: return cooldown
            return (cooldown * multiplier).toInt()
        }

    val cooldownText: MutableText
        get() = getCooldownText(cooldown)

    val defaultCooldownText: MutableText
        get() = getCooldownText(defaultCooldown)

    val availableEnhancements: Set<SkillEnhancementType<*>> =
        TODO()

    private val enhancementTooltips: MutableMap<SkillEnhancementType<*>, (SkillEnhancement) -> MutableText> =
        mutableMapOf()

    private val parameters: Map<String, SkillParameter> = settings.parameters
    private val enhancements: List<Enhancement> = settings.enhancements
    private val enhancementArgs: MutableMap<String, ((value: Float) -> Any)?> = mutableMapOf()

    /**
     * @param cooldown cooldown in seconds
     */
    internal constructor(
        id: String,
        types: List<SkillType>,
        cooldown: Int = 0,
        rarity: SkillRarity,
        sound: Supplier<SoundEvent>? = null,
        enhancements: Set<SkillEnhancementType<*>> = emptySet(),
    ) : this(Settings(id, types, cooldown, rarity))

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
            add(translate("screen.gallery.info.rarity", rarity.displayName).formatted(rarity.formatting))
            if (displayId) {
                add(id.toString().toText().formatted(Formatting.DARK_GRAY))
            }
        }

    protected fun addParameter(name: String, baseValue: Number) {
        TODO()
    }

    protected fun addEnhanceableParameter(
        name: String,
        baseValue: Number,
        enhancementId: String,
        value: Float,
        type: Enhancement.Type,
        maxLevel: Int,
        descriptionArg: (value: Float) -> Any
    ) {
        TODO()
    }

    protected fun addEnhancementDescArg(id: String, arg: (value: Float) -> Any) {
        this.enhancementArgs[id] = arg
    }

    fun getParameter(name: String): SkillParameter? = parameters[name]

    fun getIntParameter(name: String): SkillParameter.IntParameter? = getParameter(name)?.asIntParameter()

    fun getFloatParameter(name: String): SkillParameter.FloatParameter? = getParameter(name)?.asFloatParameter()

    fun getStringParameter(name: String): SkillParameter.StringParameter? = getParameter(name)?.asStringParameter()

    protected fun addEnhancement(
        id: String,
        value: Float,
        type: Enhancement.Type,
        maxLevel: Int,
        descriptionArg: (value: Float) -> Any
    ): Enhancement {
        TODO()
    }

    fun getEnhancements(): List<Enhancement> = enhancements.toList()

    fun getValidEnhancements(): List<Enhancement> = getEnhancements().filter { it.maxLevel > 0 }

    fun getEnhancement(id: String): Enhancement? = getEnhancements().firstOrNull { it.id == id }

    fun hasEnhancement(id: String): Boolean = getEnhancements().any { it.id == id }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : SkillEnhancement> addEnhancementTooltip(
        type: SkillEnhancementType<T>,
        tooltip: (T) -> MutableText
    ) {
        enhancementTooltips[type] = tooltip as (SkillEnhancement) -> MutableText
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : SkillEnhancement> addEnhancementTooltipWithArg(type: SkillEnhancementType<T>, arg: (T) -> Any) {
        enhancementTooltips[type] =
            { enhancement: T -> message(type.id, arg(enhancement)) } as (SkillEnhancement) -> MutableText
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : SkillEnhancement> addEnhancementTooltipWithArgs(
        type: SkillEnhancementType<T>,
        args: (T) -> Array<Any>
    ) {
        enhancementTooltips[type] =
            { enhancement: T -> message(type.id, *args(enhancement)) } as (SkillEnhancement) -> MutableText
    }

    abstract fun use(user: ServerPlayerEntity): UseResult

    open fun PlayerEntity.playSkillSound(exceptSelf: Boolean = false) {
        sound?.let {
            world.playSound(
                if (exceptSelf) this else null,
                blockPos,
                it.get(),
                SoundCategory.PLAYERS,
                random.nextFloat() * 0.2f + 0.9f,
                random.nextFloat() * 0.2f + 0.9f
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Skill) return false

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

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
        if (result.success) {
            player.playSkillSound()
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

    open fun applyCooldownEnhancements(player: PlayerEntity, cooldown: Int): Int =
        getEnhancedValue(player, SkillEnhancements.COOLDOWN, cooldown).coerceAtLeast(0)

    open fun getEnhancementTooltips(player: PlayerEntity): List<Text> =
        player.getEnhancements().map {
            val enhancement = it.key
            val level = it.value
            val arg = enhancementArgs[enhancement.id]?.invoke(enhancement.getValue(level))
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

    @Serializable
    data class Settings(
        @Serializable(with = IdentifierSerializer::class)
        val id: Identifier,
        var name: Text,
        var description: Text,
        @Serializable(with = IdentifierSerializer::class)
        var icon: Identifier,
        val types: Set<SkillType> = emptySet(),
        var cooldown: Int = 0,
        var rarity: SkillRarity,
        var invalid: Boolean = false,
        var weight: Int = rarity.weight,
        val parameters: MutableMap<String, SkillParameter> = mutableMapOf(),
        val enhancements: MutableList<Enhancement> = mutableListOf(),
    ) {

        constructor(
            id: String,
            types: List<SkillType>,
            cooldown: Int = 0,
            rarity: SkillRarity
        ) : this(
            id = id(id),
            name = translateSkill(id, "name"),
            description = translateSkill(id, "description"),
            icon = itemId(id),
            types = LinkedHashSet(types),
            cooldown = cooldown,
            rarity = rarity
        )

        fun addParameter(name: String, baseValue: Number): Settings {
            parameters[name] = when (baseValue) {
                is Int, is Long, is Short, is Byte -> SkillParameter.IntParameter(baseValue.toInt(), null)
                else -> SkillParameter.FloatParameter(baseValue.toFloat(), null)
            }
            return this
        }

        fun addParameter(name: String, baseValue: String): Settings {
            parameters[name] = SkillParameter.StringParameter(baseValue, null)
            return this
        }

        fun addEnhanceableParameter(
            name: String,
            baseValue: Number,
            enhancementId: String,
            value: Float,
            type: Enhancement.Type,
            maxLevel: Int
        ): Settings {
            parameters[name] = when (baseValue) {
                is Int, is Long, is Short, is Byte -> SkillParameter.IntParameter(baseValue.toInt(), enhancementId)
                else -> SkillParameter.FloatParameter(baseValue.toFloat(), enhancementId)
            }
            return addEnhancement(enhancementId, value, type, maxLevel)
        }

        fun addEnhancement(
            id: String,
            value: Float,
            type: Enhancement.Type,
            maxLevel: Int
        ): Settings {
            val name = translateSkill(this@Settings.id.path, "$id.name")
            val descriptionKey = translateSkillKey(this@Settings.id.path, "$id.description")
            val enhancement = when (type) {
                ADDITION -> Enhancement.Increment(id, name, descriptionKey, value, maxLevel)
                MULTIPLY -> Enhancement.Multiply(id, name, descriptionKey, value, maxLevel)
                LEVEL_LESS -> return addEnhancement(id)
            }
            enhancements += enhancement
            return this
        }

        fun addEnhancement(id: String): Settings {
            val enhancement = Enhancement.LevelLess(
                id,
                translateSkill(this@Settings.id.path, "$id.name"),
                translateSkillKey(this@Settings.id.path, "$id.description")
            )
            enhancements += enhancement
            return this
        }

        companion object {

            fun from(skill: Skill): Settings = Settings(
                id = skill.id,
                name = skill.name,
                description = skill.description,
                icon = skill.icon,
                types = skill.types,
                cooldown = skill.defaultCooldown,
                rarity = skill.rarity,
                invalid = skill.invalid,
                weight = skill.weight,
                parameters = skill.parameters.toMutableMap(),
                enhancements = skill.enhancements.toMutableList()
            )

            fun loadOrCreate(default: () -> Settings): Settings {
                val settings = default()
                return SkillSettingsManager.getSettings(settings.id) ?: settings
            }
        }
    }
}