package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*
import java.util.*
import java.util.function.*

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    val types: List<SkillType> = emptyList(),
    val defaultCooldown: Int = 0,
    private val rarity: Rarity,
    val sound: Supplier<SoundEvent>? = null,
    private val invalid: Boolean = false,
) : SkillTrigger {

    fun isInvalid(world: World? = null): Boolean =
        invalid || (world?.skillConfig ?: SkillConfig.instance).isInBlackList(id)

    fun getRarity(world: World? = null): Rarity =
        (world?.skillConfig ?: SkillConfig.instance).getModifier(id)?.rarity ?: rarity

    fun getFormattedName(world: World? = null): MutableText = name.copy().formatted(getRarity(world).formatting)

    fun getNameWithHoverEvent(world: World? = null): MutableText = getFormattedName(world).styled { style ->
        (item?.run {
            style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_ITEM,
                    HoverEvent.ItemStackContent(defaultStack)
                )
            )
        } ?: style)
    }

    val item: SkillItem?
        get() = Registries.ITEM[id] as? SkillItem

    fun getCooldown(world: World? = null): Int {
        val config = (world?.skillConfig ?: SkillConfig.instance)
        val cooldown = config.getModifier(id)?.cooldown ?: defaultCooldown
        val multiplier = config.skillCooldownMultiplier
        return (cooldown * multiplier).toInt()
    }

    fun getCooldownSeconds(world: World? = null): MutableText = if (getCooldown(world) <= 0) {
        translate("cooldown.none")
    } else {
        val cooldown = (getCooldown(world) / 20.0).toString()
        translate(
            "cooldown.seconds",
            if (cooldown.endsWith(".0")) cooldown.substring(0, cooldown.length - 2) else cooldown
        )
    }

    protected constructor(
        id: String,
        types: List<SkillType>,
        cooldown: Int = 0,
        rarity: Rarity,
        sound: Supplier<SoundEvent>? = null,
    ) : this(
        id(id),
        translateSkill(id, "name"),
        translateSkill(id, "description"),
        itemId(id),
        types,
        20 * cooldown,
        rarity,
        sound,
        false
    )

    fun isEmpty(): Boolean = this === Skills.EMPTY || this is EmptySkill

    fun createUuid(content: String): UUID = UUID.nameUUIDFromBytes("$id-$content".toByteArray())

    open fun getItemTooltips(world: World? = null, displayName: Boolean = false): List<Text> =
        mutableListOf<Text>().apply {
            if (displayName) {
                add(name.copy().formatted(Formatting.WHITE))
            }
            add(
                translate(
                    "screen.gallery.info.description",
                    description.string
                ).formatted(Formatting.GRAY)
            )
            add(translate(
                "screen.gallery.info.type",
                types.joinToString(" ") { it.displayName.string }
            ).formatted(Formatting.GRAY))
            add(
                translate("screen.gallery.info.cooldown", getCooldownSeconds()).formatted(
                    Formatting.GRAY
                )
            )
            add(translate("screen.gallery.info.rarity", getRarity(world).displayName.string).formatted(Formatting.GRAY))
        }

    abstract fun use(user: ServerPlayerEntity): UseResult

    open fun PlayerEntity.playSkillSound() {
        sound?.let {
            world.playSound(
                null,
                blockPos,
                it.get(),
                SoundCategory.PLAYERS,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Skill) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

    fun tryUse(
        player: ServerPlayerEntity,
        keyState: UseSkillC2SRequest.KeyState,
    ) {
        if (isInvalid(player.world)) return
        if ((this !is LongPressTrigger || !player.isUsing()) && keyState == UseSkillC2SRequest.KeyState.RELEASE) return
        if (player.isSilenced) {
            player.sendMessage(translate("useSkill.silenced"), true)
            return
        }
        if (player.isCooling() && (this !is LongPressTrigger || !player.isUsing())) {
            player.sendMessage(
                translate(
                    "useSkill.cooling",
                    name,
                    "${(player.getCooldown(this) / 20.0)}s"
                ),
                true
            )
        } else {
            player.getTriggers<UseInterruptTrigger>()
                .filter { it != this && it.shouldInterrupt(player) }
                .forEach { it.interrupt(player) }
            val result = (this as? LongPressTrigger)?.use(player, keyState) ?: use(player)
            handleResult(player, result)
        }
        return
    }

    fun handleResult(
        serverPlayerEntity: ServerPlayerEntity,
        result: UseResult,
    ) {
        if (result.success) {
            serverPlayerEntity.playSkillSound()
            serverPlayerEntity.sendMessage(result.message ?: this.name, true)
        } else {
            val message =
                result.message ?: translate("useSkill.failed", name)
            if (message != Text.empty())
                serverPlayerEntity.sendMessage(message, true)
        }
        if (result.cooling) {
            serverPlayerEntity.startCooling()
            (this as? SynchronousCoolingTrigger)?.getOtherSkills(serverPlayerEntity)
                ?.forEach(serverPlayerEntity::startCooling)
        }
    }

    override fun getAsSkill(): Skill = this

    open fun isDangerous(player: ServerPlayerEntity): Boolean = false

    fun failedMessage() = translateSkill(id.path, "failed")

    fun message(key: String, vararg args: Any) = translateSkill(id.path, key, *args)

    enum class Rarity(
        val level: Int,
        val id: String,
        val formatting: Formatting,
    ) {

        USELESS(0, "useless", Formatting.GRAY),
        COMMON(1, "common", Formatting.WHITE),
        UNCOMMON(2, "uncommon", Formatting.GREEN),
        RARE(3, "rare", Formatting.AQUA),
        SUPERB(4, "superb", Formatting.GOLD),
        EPIC(5, "epic", Formatting.RED),
        LEGENDARY(6, "legendary", Formatting.LIGHT_PURPLE),
        MYTHIC(7, "mythic", Formatting.DARK_PURPLE),
        UNIQUE(8, "unique", Formatting.DARK_RED);

        val displayName: Text
            get() = translate("skillRarity.$id")

        companion object {

            fun fromLevel(level: Int): Rarity? = entries.find { it.level == level }

            fun fromId(id: String): Rarity? = entries.find { it.id == id }

            fun parse(string: String): Rarity? = try {
                valueOf(string)
            } catch (e: Exception) {
                fromId(string) ?: string.toIntOrNull()?.let { fromLevel(it) }
            }
        }
    }
}