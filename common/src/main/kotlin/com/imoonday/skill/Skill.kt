package com.imoonday.skill

import com.imoonday.*
import com.imoonday.config.*
import com.imoonday.init.*
import com.imoonday.item.*
import com.imoonday.network.*
import com.imoonday.trigger.*
import com.imoonday.util.*
import com.mojang.blaze3d.systems.*
import net.minecraft.client.*
import net.minecraft.client.gui.*
import net.minecraft.client.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.*
import java.awt.*
import java.util.*
import kotlin.math.*

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    val types: List<SkillType> = emptyList(),
    val defaultCooldown: Int = 0,
    rarity: Rarity,
    val sound: SoundEvent? = null,
    invalid: Boolean = false,
) : SkillTrigger {

    val invalid = invalid
        get() = Config.instance.skillBlackList[id.namespace]?.contains(id.path) == true || field
    val rarity = rarity
        get() = Config.instance.skillModifier[id.namespace]
            ?.get(id.path)
            ?.get("rarity")
            ?.let { Rarity.parse(it) }
            ?: field
    val formattedName: Text
        get() = name.copy().formatted(rarity.formatting)
    val item: SkillItem?
        get() = Registries.ITEM[id] as? SkillItem
    val modelId
        get() = ModelIdentifier(Registries.ITEM.getId(item), "inventory")

    constructor(
        id: String,
        types: List<SkillType>,
        cooldown: Int = 0,
        rarity: Rarity,
        sound: SoundEvent? = null,
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

    fun isEmpty(): Boolean = this == EMPTY

    fun getCooldown(world: World?): Int =
        ((Config.instance.skillModifier[id.namespace]
            ?.get(id.path)
            ?.get("cooldown")
            ?.toIntOrNull()
            ?: defaultCooldown) *
            ((world?.gameRules
                ?.get(ModGameRules.COOLDOWN_MULTIPLIER)
                ?.get()
                ?.div(100.0)) ?: 1.0)
                .coerceIn(0.0, 1.0)
            ).toInt()

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
                ).map { it.string.toText().formatted(Formatting.GRAY) }
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
    open fun playSoundFrom(player: PlayerEntity) {
        sound?.let {
            player.world.playSound(
                null,
                player.blockPos,
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
            playSoundFrom(serverPlayerEntity)
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

    override fun getAsSkill(): Skill = this

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

    fun failedMessage() = translateSkill(id.path, "failed")

    fun message(key: String) = translateSkill(id.path, key)

    open fun render(
        context: DrawContext,
        x: Int,
        y: Int,
        player: PlayerEntity,
    ) {
        val endY = y + 16
        renderIcon(context, x, y, player)
        renderProgressBar(context, x, endY, 16, 1, player)
        renderCooldownOverlay(context, x, endY, 16, 16, player)
    }

    open fun renderIcon(
        context: DrawContext,
        x: Int,
        y: Int,
        player: PlayerEntity?,
    ) {
        context.fill(x, y, x + 16, y + 16, Color.LIGHT_GRAY.alpha(0.5).rgb)
        var flashed = false
        if (this is AutoStopTrigger && shouldFlashIcon() && player != null) {
            flashed = true
            val persistTime = getPersistTime()
            val leftUseTime = persistTime - player.getUsedTime(this)
            if (persistTime > 20 * 5 && leftUseTime <= persistTime / 5) {
                val alpha = 0.5 * sin(2 * PI / 20 * (leftUseTime - persistTime / 5)) + 0.5
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha.toFloat())
            }
        }
        if (!isEmpty()) context.drawTexture(icon, x, y, 0f, 0f, 16, 16, 16, 16)
        if (flashed) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.disableBlend()
        }
        if (player?.isSilenced == true) context.fill(x, y, x + 16, y + 16, Color.RED.alpha(0.25).rgb)
    }

    open fun renderProgressBar(
        context: DrawContext,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        player: PlayerEntity,
    ) {
        if (invalid) return
        if (this is ProgressTrigger && shouldDisplay(player)
            && (player.isUsing() || this !is UsingProgressTrigger)
        ) {
            val progress = getProgress(player)
            val centerX = x + (width * progress).toInt()
            context.fill(x, y, centerX, y + height, progressColor)
            context.fill(centerX, y, x + width, y + height, Color.GRAY.rgb)
        }
    }

    open fun renderCooldownOverlay(
        context: DrawContext,
        startX: Int,
        endY: Int,
        width: Int,
        maxHeight: Int,
        player: PlayerEntity,
    ) {
        if (!player.isCooling()) return
        val cooldown = player.getCooldown(this)
        val maxCooldown = getCooldown(player.world)
        val progress = (cooldown.toDouble() / maxCooldown).coerceIn(0.0, 1.0)
        val startY = (endY - progress * maxHeight).toInt()
        context.fill(startX, startY, startX + width, endY, Color.BLACK.alpha(0.25).rgb)
        if (cooldown < 20 * 4) {
            val time = if (cooldown <= 20) String.format("%.1f", cooldown / 20.0) else (cooldown / 20).toString()
            context.drawCenteredTextWithShadow(
                client!!.textRenderer,
                time,
                (startX + width / 2.0).toInt(),
                (endY - width / 2.0).toInt(),
                Color.WHITE.rgb
            )
        }
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

    companion object {

        private val skills = mutableSetOf<Skill>()

        @JvmField
        val EMPTY = EmptySkill().register()
        private val progressColor = Color(128, 255, 130).rgb
        fun getSkills() = skills.toList()
        fun getValidSkills() = skills.filterNot { it.invalid }
        fun fromId(id: Identifier?) = skills.find { it.id == id } ?: EMPTY
        fun fromId(id: String?) = skills.find { it.id == id?.toIdentifier() } ?: EMPTY
        fun fromIdNullable(id: Identifier?) = skills.find { it.id == id }
        fun fromIdNullable(id: String?) = skills.find { it.id == Identifier.tryParse(id) }
        inline fun <reified T : SkillTrigger> getTriggers(predicate: (T) -> Boolean = { true }): List<T> =
            getSkills().filterIsInstance<T>().filter(predicate)

        fun getLearnableSkills(
            except: Collection<Skill> = emptyList(),
            filter: (Skill) -> Boolean = { true },
        ): List<Skill> = getValidSkills()
            .filterNot { it in except }
            .filter(filter)

        fun random(except: Collection<Skill> = emptyList(), filter: (Skill) -> Boolean = { true }): Skill =
            getLearnableSkills(except, filter).randomOrNull() ?: EMPTY
    }
}