package com.imoonday.skill

import com.imoonday.LOGGER
import com.imoonday.component.getCooldown
import com.imoonday.component.isCooling
import com.imoonday.component.startCooling
import com.imoonday.config.Config
import com.imoonday.init.ModGameRules
import com.imoonday.init.ModSkills.EMPTY
import com.imoonday.init.isSilenced
import com.imoonday.item.SkillItem
import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.trigger.LongPressTrigger
import com.imoonday.trigger.SkillTrigger
import com.imoonday.trigger.SynchronousCoolingTrigger
import com.imoonday.util.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.*

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    vararg val types: SkillType = emptyArray(),
    private val defaultCooldown: Int = 0,
    rarity: Rarity,
    val sound: SoundEvent? = null,
    invalid: Boolean = false,
) : SkillTrigger {

    val invalid = invalid
        get() = Config.instance.skillBlackList[id.namespace]?.contains(id.path) == true || field
    val rarity = rarity
        get() = Config.instance.rarityModifier[id.namespace]?.get(id.path) ?: field
    val formattedName: Text
        get() = name.copy().formatted(rarity.formatting)
    val item: SkillItem?
        get() = Registries.ITEM[id] as? SkillItem
    val modelId
        get() = ModelIdentifier(Registries.ITEM.getId(item), "inventory")

    constructor(
        id: String,
        vararg types: SkillType,
        cooldown: Int = 0,
        rarity: Rarity,
        sound: SoundEvent? = null,
    ) : this(
        id(id),
        translateSkill(id, "name"),
        translateSkill(id, "description"),
        itemId(id),
        types = types,
        20 * cooldown,
        rarity,
        sound,
        false
    )

    fun getCooldown(world: World?): Int =
        ((Config.instance.cooldownModifier[id.namespace]?.get(id.path) ?: defaultCooldown) *
            (world?.gameRules?.get(ModGameRules.COOLDOWN_MULTIPLIER)?.get() ?: 1.0)).toInt()

    fun createUuid(content: String): UUID = UUID.nameUUIDFromBytes("$id-$content".toByteArray())

    open fun getItemTooltips(client: MinecraftClient, displayName: Boolean = false): List<Text> {
        return mutableListOf<Text>().apply {
            if (displayName) {
                add(name.copy().formatted(Formatting.WHITE))
            }
            addAll(
                client.textRenderer.textHandler.wrapLines(
                    translate(
                        "screen",
                        "gallery.info.description",
                        description.string
                    ).formatted(Formatting.GRAY),
                    170,
                    Style.EMPTY
                ).map { Text.literal(it.string).formatted(Formatting.GRAY) }
            )
            add(translate(
                "screen",
                "gallery.info.type",
                types.joinToString(" ") { it.displayName.string }
            ).formatted(Formatting.GRAY))
            add(
                translate("screen", "gallery.info.cooldown", "${getCooldown(client.world) / 20.0}s").formatted(
                    Formatting.GRAY
                )
            )
            add(translate("screen", "gallery.info.rarity", rarity.displayName.string).formatted(Formatting.GRAY))
        }
    }

    abstract fun use(user: ServerPlayerEntity): UseResult
    open fun playSound(user: PlayerEntity) {
        sound?.let {
            user.world.playSound(
                null,
                user.blockPos,
                it,
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
    protected fun reflectedFailed(player: ServerPlayerEntity) {
        player.sendMessage(translateSkill("extreme_reflection", "failed"), true)
    }

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.playSound(SoundEvents.ITEM_SHIELD_BLOCK)
        attacker?.damage(player.damageSources.thorns(player), amount)?.let {
            player.sendMessage(
                translateSkill("extreme_reflection", if (it) "success" else "failed"),
                true
            )
        }
    }

    fun tryUse(
        player: ServerPlayerEntity,
        keyState: UseSkillC2SRequest.KeyState,
    ) {
        if (invalid) return
        if ((this !is LongPressTrigger || !player.isUsing()) && keyState == UseSkillC2SRequest.KeyState.RELEASE) return
        if (player.isSilenced) {
            player.sendMessage(translate("useSkill", "silenced"), true)
            return
        }
        if (player.isCooling(this) && (this !is LongPressTrigger || !player.isUsing())) {
            player.sendMessage(
                translate("useSkill", "cooling", name.string, "${(player.getCooldown(this) / 20.0)}s"),
                true
            )
        } else {
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
            playSound(serverPlayerEntity)
            serverPlayerEntity.sendMessage(result.message ?: this.name, true)
        } else {
            val message =
                result.message ?: translate("useSkill", "failed", name.string)
            if (message != Text.empty())
                serverPlayerEntity.sendMessage(message, true)
        }
        if (result.cooling) {
            serverPlayerEntity.startCooling()
            (this as? SynchronousCoolingTrigger)?.getOtherSkills()?.forEach { serverPlayerEntity.startCooling(it) }
        }
    }

    override fun asSkill(): Skill = this

    open fun isDangerousTo(player: ServerPlayerEntity): Boolean = false

    fun register(): Skill {
        if (this in skills) {
            LOGGER.warn("Skill $id is already registered")
            return this
        }
        if (!invalid) Registry.register(Registries.ITEM, id, SkillItem(this))
        skills.add(this)
        return this
    }

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
            get() = translate("skillRarity", id)
    }

    companion object {

        private val skills = mutableSetOf<Skill>()
        fun getSkills() = skills.toList()
        fun getValidSkills() = skills.filterNot { it.invalid }
        fun fromId(id: Identifier) = skills.find { it.id == id } ?: EMPTY
        fun fromIdNullable(id: Identifier) = skills.find { it.id == id }
    }
}