package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.enhancement.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import java.util.*
import java.util.function.*

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    val types: List<SkillType> = emptyList(),
    val defaultCooldown: Int = 0,
    rarity: SkillRarity,
    val sound: Supplier<SoundEvent>? = null,
    enhancements: Set<SkillEnhancementType<*>> = emptySet(),
    invalid: Boolean = false,
) : SkillTrigger {

    val invalid: Boolean = invalid
        get() = field || SkillConfig.get().isInBlackList(id) || GlobalConfig.get().skillConfig.isInBlackList(id)

    val rarity: SkillRarity = rarity
        get() = SkillConfig.get().getModifier(id)?.rarity
            ?: GlobalConfig.get().skillConfig.getModifier(id)?.rarity
            ?: field

    open val weight: Int
        get() = rarity.weight

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
            val config = SkillConfig.get()
            val globalConfig = GlobalConfig.get()
            val cooldown = config.getModifier(id)?.cooldown
                ?: globalConfig.skillConfig.getModifier(id)?.cooldown
                ?: defaultCooldown
            val multiplier = config.skillCooldownMultiplier
                ?: globalConfig.skillConfig.skillCooldownMultiplier
                ?: return cooldown
            return (cooldown * multiplier).toInt()
        }

    val cooldownText: MutableText
        get() = getCooldownText(cooldown)

    val defaultCooldownText: MutableText
        get() = getCooldownText(defaultCooldown)

    val availableEnhancements: Set<SkillEnhancementType<*>> =
        if (defaultCooldown > 0) enhancements + SkillEnhancements.COOLDOWN
        else enhancements

    private val enhancementTooltips: MutableMap<SkillEnhancementType<*>, (SkillEnhancement) -> MutableText> =
        mutableMapOf()

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
    ) : this(
        id(id),
        translateSkill(id, "name"),
        translateSkill(id, "description"),
        itemId(id),
        types,
        20 * cooldown,
        rarity,
        sound,
        enhancements,
        false
    )

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

    fun <T : SkillEnhancement> getEnhancementTooltip(enhancement: T): MutableText =
        enhancementTooltips[enhancement.type]?.invoke(enhancement) ?: enhancement.description.copy()

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

    open fun applyCooldownEnhancements(player: PlayerEntity, cooldown: Int): Int =
        getEnhancedValue(player, SkillEnhancements.COOLDOWN, cooldown).coerceAtLeast(0)

    open fun getEnhancementTooltips(player: PlayerEntity): List<Text> =
        player.getEnhancements().map { getEnhancementTooltip(it).formatted(Formatting.BLUE) }

    fun isAvailable(enhancement: SkillEnhancement): Boolean =
        availableEnhancements.contains(enhancement.type)

    fun isAvailableFor(player: PlayerEntity, enhancement: SkillEnhancement): Boolean =
        isAvailable(enhancement) && player.getEnhancement(enhancement.type)
            ?.let { enhancement.level > it.level } != false

    fun getCooldownText(cooldown: Int) = if (cooldown <= 0) {
        translate("cooldown.none")
    } else {
        val text = (cooldown / 20.0).toString()
        translate(
            "cooldown.seconds",
            if (text.endsWith(".0")) text.substring(0, text.length - 2) else text
        )
    }
}