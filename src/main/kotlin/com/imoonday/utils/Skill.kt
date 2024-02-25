package com.imoonday.utils

import com.imoonday.AdvancedSkills
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
    val icon: Identifier = AdvancedSkills.id("unknown.png"),
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
        AdvancedSkills.id(id),
        TranslationUtil.skillName(id),
        TranslationUtil.skillDescription(id),
        AdvancedSkills.itemPath(id),
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
        player.sendMessage(Text.translatable("advancedSkills.skill.extreme_reflection.failed"), true)
    }

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS)
        attacker?.damage(player.damageSources.thorns(player), amount)?.let {
            player.sendMessage(
                Text.translatable("advancedSkills.skill.extreme_reflection.${if (it) "success" else "failed"}"),
                true
            )
        }
    }

    enum class Rarity(
        val level: Int,
        private val translationKey: String,
        val formatting: Formatting,
    ) {
        USELESS(0, "advancedSkills.skillRarity.useless", Formatting.WHITE),
        COMMON(1, "advancedSkills.skillRarity.common", Formatting.GRAY),
        UNCOMMON(2, "advancedSkills.skillRarity.uncommon", Formatting.BLUE),
        RARE(3, "advancedSkills.skillRarity.rare", Formatting.GOLD),
        VERY_RARE(4, "advancedSkills.skillRarity.veryRare", Formatting.RED),
        EPIC(5, "advancedSkills.skillRarity.epic", Formatting.LIGHT_PURPLE),
        LEGENDARY(6, "advancedSkills.skillRarity.legendary", Formatting.DARK_PURPLE),
        MYTHIC(7, "advancedSkills.skillRarity.mythic", Formatting.DARK_RED),
        UNIQUE(8, "advancedSkills.skillRarity.unique", Formatting.BLACK);

        val displayName: Text
            get() = Text.translatable(translationKey)
    }
}