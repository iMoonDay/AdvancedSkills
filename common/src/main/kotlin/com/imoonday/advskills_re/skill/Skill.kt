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
import java.util.*
import java.util.function.*

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    val types: List<SkillType> = emptyList(),
    cooldown: Int = 0,
    rarity: Rarity,
    val sound: Supplier<SoundEvent>? = null,
    invalid: Boolean = false,
) : SkillTrigger {

    val invalid: Boolean = invalid
        get() = field || SkillConfig.get().isInBlackList(id)

    val rarity: Rarity = rarity
        get() = SkillConfig.get().getModifier(id)?.rarity ?: field

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

    val cooldown: Int = cooldown
        get() {
            val config = SkillConfig.get()
            val cooldown = config.getModifier(id)?.cooldown ?: field
            val multiplier = config.skillCooldownMultiplier
            return (cooldown * multiplier).toInt()
        }

    val cooldownText: MutableText
        get() = if (cooldown <= 0) {
            translate("cooldown.none")
        } else {
            val text = (cooldown / 20.0).toString()
            translate(
                "cooldown.seconds",
                if (text.endsWith(".0")) text.substring(0, text.length - 2) else text
            )
        }

    /**
     * @param cooldown cooldown in seconds
     */
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

    open fun getItemTooltips(displayName: Boolean = false, displayId: Boolean = false): List<Text> =
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

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

    fun tryUse(
        player: ServerPlayerEntity,
        keyState: UseSkillC2SRequest.KeyState,
    ) {
        if (invalid) return
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
                    translate("cooldown.seconds", player.getCooldown(this) / 20.0)
                ),
                true
            )
        } else {
            player.forEachTrigger<UseInterruptTrigger>(
                { it != this && it.shouldInterrupt(player) }
            ) { it.interrupt(player) }
            val result = (this as? LongPressTrigger)?.use(player, keyState) ?: use(player)
            handleResult(player, result)
        }
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
        val weight: Int,
        val id: String,
        val roman: String,
        val formatting: Formatting,
    ) {

        USELESS(0, 0, "useless", "N", Formatting.GRAY),
        COMMON(1, 8, "common", "I", Formatting.WHITE),
        UNCOMMON(2, 7, "uncommon", "II", Formatting.GREEN),
        RARE(3, 6, "rare", "III", Formatting.AQUA),
        SUPERB(4, 5, "superb", "IV", Formatting.GOLD),
        EPIC(5, 4, "epic", "V", Formatting.RED),
        LEGENDARY(6, 3, "legendary", "VI", Formatting.LIGHT_PURPLE),
        MYTHIC(7, 2, "mythic", "VII", Formatting.DARK_PURPLE),
        UNIQUE(8, 1, "unique", "VIII", Formatting.DARK_RED);

        val displayName: Text
            get() = translate("skillRarity.$id")

        val color: Int
            get() = formatting.colorValue!!

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