package com.imoonday.utils

import com.imoonday.components.*
import com.imoonday.init.isSilenced
import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.triggers.LongPressTrigger
import com.imoonday.triggers.SynchronousCoolingTrigger
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    vararg val types: SkillType,
    val cooldown: Int = 0,
    val rarity: Rarity,
    val sound: SoundEvent? = null,
    val isEmpty: Boolean = false,
) {

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

    val formattedName: Text
        get() = name.copy().formatted(rarity.formatting)
    val item: SkillItem?
        get() = Registries.ITEM[id] as? SkillItem

    open val tooltips: List<Text>
        get() = listOf(
            name,
            Text.literal(types.joinToString(" ") { it.displayName.string }),
            description,
            Text.literal("${cooldown / 20.0}s")
        )

    abstract fun use(user: ServerPlayerEntity): UseResult

    open fun playSound(user: PlayerEntity) {
        sound?.let {
            user.world.playSound(
                null,
                user.blockPos,
                it,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Skill) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    protected fun reflectedFailed(player: ServerPlayerEntity) {
        player.sendMessage(translateSkill("extreme_reflection", "failed"), true)
    }

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS)
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
        if ((this !is LongPressTrigger || !player.isUsingSkill(this)) && keyState == UseSkillC2SRequest.KeyState.RELEASE) return
        if (player.isSilenced) {
            player.sendMessage(translate("useSkill", "silenced"), true)
            return
        }
        if (player.isCooling(this)) {
            player.sendMessage(
                translate("useSkill", "cooling", name.string, "${(player.getCooldown(this) / 20.0)}s"),
                true
            )
        } else {
            val result = (this as? LongPressTrigger)?.let {
                if (keyState == UseSkillC2SRequest.KeyState.PRESS) it.onPress(player) else it.onRelease(
                    player,
                    player.getSkillUsedTime(this)
                )
            } ?: use(player)
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
            serverPlayerEntity.startCooling(this)
            (this as? SynchronousCoolingTrigger)?.otherSkills?.forEach { serverPlayerEntity.startCooling(it) }
        }
    }

    enum class Rarity(
        val level: Int,
        val id: String,
        val formatting: Formatting,
    ) {
        USELESS(0, "useless", Formatting.WHITE),
        COMMON(1, "common", Formatting.GRAY),
        UNCOMMON(2, "uncommon", Formatting.BLUE),
        RARE(3, "rare", Formatting.GOLD),
        VERY_RARE(4, "veryRare", Formatting.RED),
        EPIC(5, "epic", Formatting.LIGHT_PURPLE),
        LEGENDARY(6, "legendary", Formatting.DARK_PURPLE),
        MYTHIC(7, "mythic", Formatting.DARK_RED),
        UNIQUE(8, "unique", Formatting.BLACK);

        val displayName: Text
            get() = translate("skillRarity", id)
    }
}