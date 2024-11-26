package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.init.ModItems.ITEMS
import com.imoonday.advskills_re.item.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import com.mojang.blaze3d.systems.*
import com.mojang.logging.*
import net.minecraft.client.gui.*
import net.minecraft.client.util.*
import net.minecraft.entity.player.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.text.*
import net.minecraft.util.*
import org.slf4j.*
import java.awt.*
import java.util.*
import java.util.function.*
import kotlin.math.*

abstract class Skill(
    val id: Identifier,
    val name: Text,
    val description: Text,
    val icon: Identifier = id("unknown.png"),
    val types: List<SkillType> = emptyList(),
    val defaultCooldown: Int = 0,
    rarity: Rarity,
    val sound: Supplier<SoundEvent>? = null,
    invalid: Boolean = false,
) : SkillTrigger {

    val invalid = invalid
        get() = field || SkillConfig.instance.isInBlackList(id)
    val rarity = rarity
        get() = SkillConfig.instance.getModifier(id)?.rarity ?: field
    val formattedName: MutableText
        get() = name.copy().formatted(rarity.formatting)
    val item: SkillItem?
        get() = Registries.ITEM[id] as? SkillItem
    val modelId
        get() = ModelIdentifier(Registries.ITEM.getId(item), "inventory")
    val cooldown: Int
        get() = ((SkillConfig.instance.getModifier(id)?.cooldown ?: defaultCooldown) *
            SkillConfig.instance.skillCooldownMultiplier
            ).toInt()
    val cooldownSeconds: MutableText
        get() = if (cooldown <= 0) {
            translate("cooldown.none")
        } else {
            val cooldown = (cooldown / 20.0).toString()
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

    fun isEmpty(): Boolean = this == EMPTY
    fun createUuid(content: String): UUID = UUID.nameUUIDFromBytes("$id-$content".toByteArray())
    open fun getItemTooltips(displayName: Boolean = false): List<Text> = mutableListOf<Text>().apply {
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
            translate("screen.gallery.info.cooldown", cooldownSeconds).formatted(
                Formatting.GRAY
            )
        )
        add(translate("screen.gallery.info.rarity", rarity.displayName.string).formatted(Formatting.GRAY))
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
                    name.string,
                    "${(player.getCooldown(this) / 20.0)}s"
                ),
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
            serverPlayerEntity.playSkillSound()
            serverPlayerEntity.sendMessage(result.message ?: this.name, true)
        } else {
            val message =
                result.message ?: translate("useSkill.failed", name.string)
            if (message != Text.empty())
                serverPlayerEntity.sendMessage(message, true)
        }
        if (result.cooling) {
            serverPlayerEntity.startCooling()
            (this as? SynchronousCoolingTrigger)?.getOtherSkills()
                ?.forEach(serverPlayerEntity::startCooling)
        }
    }

    override fun getAsSkill(): Skill = this
    open fun isDangerous(player: ServerPlayerEntity): Boolean = false
    fun register(): Skill {
        if (this in skills) {
            LOGGER.warn("Skill $id is already registered")
            return this
        }
        if (!invalid) ITEMS.register(id.path) { SkillItem(this) }
        skills.add(this)
        return this
    }

    fun failedMessage() = translateSkill(id.path, "failed")
    fun message(key: String, vararg args: Any) = translateSkill(id.path, key, *args)
    open fun render(
        context: DrawContext,
        x: Int,
        y: Int,
        player: PlayerEntity,
    ) {
        val endY = y + 16
        renderIcon(context, x, y, player)
        renderProgressBar(context, x, endY - 1, 16, 1, player)
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
            val persistTime = getPersistTimeModified()
            val leftUseTime = persistTime - player.getUsedTime(this)
            if (persistTime > 20 * 5 && leftUseTime <= persistTime / 5) {
                val alpha = 0.5 * sin(2 * PI / 20 * (leftUseTime - persistTime / 5)) + 0.5
                RenderSystem.enableBlend()
                context.setShaderColor(1.0f, 1.0f, 1.0f, alpha.toFloat())
            }
        }
        if (!isEmpty()) renderIcon(context, x, y)
        if (flashed) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.disableBlend()
        }
        if (player?.isSilenced == true) context.fill(x, y, x + 16, y + 16, Color.RED.alpha(0.25).rgb)
    }

    fun renderIcon(
        context: DrawContext,
        x: Int,
        y: Int,
        size: Int = 16,
    ) = context.drawTexture(icon, x, y, 0f, 0f, size, size, size, size)

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
            val progress = getProgress(player).coerceIn(0.0, 1.0)
            val centerX = x + (width * progress).toInt()
            context.fill(x, y, centerX, y + height, 0xFF00BFFF.toInt())
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
        val maxCooldown = this.cooldown
        val progress = (cooldown.toDouble() / maxCooldown).coerceIn(0.0, 1.0)
        val startY = (endY - progress * maxHeight).toInt()
        context.fill(startX, startY, startX + width, endY, Color.BLACK.alpha(0.25).rgb)
        if (cooldown < 20 * 4) {
            val time = if (cooldown <= 20) String.format("%.1f", cooldown / 20.0) else (cooldown / 20).toString()
            val textRenderer = client!!.textRenderer
            context.matrices.push()
            context.matrices.translate(
                startX + (width - textRenderer.getWidth(time)) / 2.0 + 0.5,
                endY - width / 2.0,
                0.0
            )
            context.drawText(textRenderer, time, 0, 0, 0xFFFFFF, false)
            context.matrices.pop()
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

    companion object {

        private val LOGGER: Logger = LogUtils.getLogger()
        private val skills = mutableSetOf<Skill>()
        val triggers: MutableMap<Class<out SkillTrigger>, List<SkillTrigger>> = mutableMapOf()

        @JvmField
        val EMPTY = EmptySkill().register()
        fun getSkills() = skills.toList()
        fun getValidSkills() = skills.filterNot { it.invalid }
        fun fromId(id: Identifier?) = skills.find { it.id == id } ?: EMPTY
        fun fromId(id: String?) = skills.find { it.id == id?.toIdentifier() } ?: EMPTY
        fun fromIdNullable(id: Identifier?) = skills.find { it.id == id }
        fun fromIdNullable(id: String?) = skills.find { it.id == Identifier.tryParse(id) }

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : SkillTrigger> getTriggers(predicate: (T) -> Boolean = { true }): List<T> {
            val triggers: List<T> = (triggers[T::class.java] ?: getSkills().filterIsInstance<T>().also {
                triggers[T::class.java] = it
            }) as List<T>
            return triggers.filter(predicate)
        }

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