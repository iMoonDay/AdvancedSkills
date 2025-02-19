package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.ClientConfig.Companion.DEFAULT_LAYOUT_STRING_LIST
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import me.shedaniel.clothconfig2.api.*
import me.shedaniel.clothconfig2.gui.entries.*
import net.minecraft.client.gui.screen.*
import java.util.*

class ModConfigScreenFactory {

    private var cache: Screen? = null
    private val clientConfig = ClientConfig.get()
    private val globalConfig = GlobalConfig.get()
    private val modifiedSettings = mutableMapOf<Skill, Skill.Settings>()

    fun createScreen(parent: Screen?): Screen? {
        if (!AdvancedSkillsClient.clothConfigLoaded) return parent
        if (cache != null) return cache
        try {
            val builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(translate("screen.config.title"))
                .setSavingRunnable(::save)

            val entryBuilder = builder.entryBuilder()

            addRenderCategory(builder, entryBuilder)
            addGeneralCategory(builder, entryBuilder)
            addGlobalCategory(builder, entryBuilder)
            addSkillEditorCategory(builder, entryBuilder)

            return builder.build().also { cache = it }
        } catch (e: Exception) {
            LOGGER.error("Error while creating config screen", e)
            return parent
        }
    }

    private fun save() {
        clientConfig.save()
        globalConfig.save()
        modifiedSettings.forEach { (_, settings) ->
            SettingsManager.saveSettings(settings)
        }

        val client = client
        if (client?.isIntegratedServerRunning == true) {
            client.server?.let {
                Channels.SYNC_CONFIG_S2C.sendToPlayers(
                    it.playerManager.playerList,
                    SyncConfigS2CPacket(globalConfig.toNbt(), SyncConfigS2CPacket.ConfigType.GLOBAL)
                )
                if (modifiedSettings.isNotEmpty()) {
                    Channels.SYNC_SETTINGS_S2C.sendToPlayers(
                        it.playerManager.playerList,
                        SyncSettingsS2CPacket(modifiedSettings.values.toList())
                    )
                }
            }
        }
    }

    private fun addSkillEditorCategory(builder: ConfigBuilder, entryBuilder: ConfigEntryBuilder) {
        SettingsManager.loadFiles()

        builder.getOrCreateCategory(translate("screen.config.category.editor")).run {

            addEntry(
                entryBuilder.startTextDescription(translate("screen.config.skillSettingsLocation")).build()
            )

            Skills.getSkills().forEach {
                val entry = createEntry(entryBuilder, it)
                if (entry != null) {
                    addEntry(entry)
                }
            }
        }
    }

    private fun createEntry(
        builder: ConfigEntryBuilder,
        skill: Skill
    ): SubCategoryListEntry? {
        val defaultSettings = Skills.createDefaultSkill(skill.id)?.settings ?: return null
        val settings = Skill.Settings(SettingsManager.getSettings(skill) ?: return null)
        return builder.startSubCategory(skill.name)
            .setExpanded(false)
            .apply {
                add(builder.startIntField(translate("screen.config.skillParameter.cooldown"), settings.cooldown)
                    .setDefaultValue(defaultSettings.cooldown)
                    .setMin(0)
                    .setSaveConsumer {
                        if (it != settings.cooldown) {
                            settings.cooldown = it
                            modifiedSettings.putIfAbsent(skill, settings)
                        }
                    }
                    .build()
                )

                add(builder.startSelector(
                    translate("screen.config.skillParameter.rarity"), SkillRarity.rarities.toTypedArray(),
                    settings.rarity
                )
                    .setDefaultValue(defaultSettings.rarity)
                    .setNameProvider { it.format(it.displayName) }
                    .setSaveConsumer {
                        if (it != settings.rarity) {
                            settings.rarity = it
                            modifiedSettings.putIfAbsent(skill, settings)
                        }
                    }
                    .build()
                )

                add(builder.startBooleanToggle(translate("screen.config.skillParameter.disabled"), settings.disabled)
                    .setDefaultValue(defaultSettings.disabled)
                    .setSaveConsumer {
                        if (it != settings.disabled) {
                            settings.disabled = it
                            modifiedSettings.putIfAbsent(skill, settings)
                        }
                    }
                    .build()
                )

                add(builder.startIntField(translate("screen.config.skillParameter.weight"), settings.weight)
                    .setDefaultValue(defaultSettings.weight)
                    .setMin(0)
                    .setSaveConsumer {
                        if (it != settings.weight) {
                            settings.weight = it
                            modifiedSettings.putIfAbsent(skill, settings)
                        }
                    }
                    .build()
                )

                add(builder.startBooleanToggle(translate("screen.config.skillParameter.drawable"), settings.drawable)
                    .setDefaultValue(defaultSettings.drawable)
                    .setSaveConsumer {
                        if (it != settings.drawable) {
                            settings.drawable = it
                            modifiedSettings.putIfAbsent(skill, settings)
                        }
                    }
                    .build()
                )
            }
            .build()
    }

    private fun addGlobalCategory(
        builder: ConfigBuilder,
        entryBuilder: ConfigEntryBuilder
    ) {
        builder.getOrCreateCategory(translate("screen.config.category.global")).run {

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.initialDrawTimes"),
                    globalConfig.initialDrawTimes
                ).setDefaultValue(5)
                    .setMin(0)
                    .setSaveConsumer { globalConfig.initialDrawTimes = it }
                    .build()
            )

            val defaultSlots =
                entryBuilder.startSubCategory(translate("screen.config.defaultSkillSlots.subCategory"))

            defaultSlots.add(
                entryBuilder.startIntSlider(
                    translate("screen.config.defaultSkillSlots.active"),
                    globalConfig.defaultSkillSlots.getOrDefault("active", 0),
                    0, 10
                ).setDefaultValue(SkillContainer.DEFAULT_SLOTS["active"])
                    .setSaveConsumer { globalConfig.setDefaultSkillSlot("active", it) }
                    .build()
            )

            defaultSlots.add(
                entryBuilder.startIntSlider(
                    translate("screen.config.defaultSkillSlots.generic"),
                    globalConfig.defaultSkillSlots.getOrDefault("generic", 0),
                    0, 10
                ).setDefaultValue(SkillContainer.DEFAULT_SLOTS["generic"])
                    .setSaveConsumer { globalConfig.setDefaultSkillSlot("generic", it) }
                    .build()
            )

            defaultSlots.add(
                entryBuilder.startIntSlider(
                    translate("screen.config.defaultSkillSlots.passive"),
                    globalConfig.defaultSkillSlots.getOrDefault("passive", 0),
                    0, 10
                ).setDefaultValue(SkillContainer.DEFAULT_SLOTS["passive"])
                    .setSaveConsumer { globalConfig.setDefaultSkillSlot("passive", it) }
                    .build()
            )

            addEntry(defaultSlots.build())

            val skillFruitGeneration =
                entryBuilder.startSubCategory(translate("screen.config.skillFruitGeneration"))

            skillFruitGeneration.add(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.disableSkillFruitGeneration"),
                    globalConfig.disableSkillFruitGeneration
                ).setDefaultValue(false)
                    .setSaveConsumer { globalConfig.disableSkillFruitGeneration = it }
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startTextDescription(translate("screen.config.generationChance"))
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startFloatField(
                    translate("screen.config.oakLeavesDropChance"),
                    globalConfig.oakLeavesDropChance,
                ).setDefaultValue(0.005f)
                    .setMin(0f)
                    .setMax(1f)
                    .setSaveConsumer { globalConfig.oakLeavesDropChance = it }
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startFloatField(
                    translate("screen.config.darkOakLeavesDropChance"),
                    globalConfig.darkOakLeavesDropChance,
                ).setDefaultValue(0.005f)
                    .setMin(0f)
                    .setMax(1f)
                    .setSaveConsumer { globalConfig.darkOakLeavesDropChance = it }
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startFloatField(
                    translate("screen.config.ancientCityChestGenerationChance"),
                    globalConfig.ancientCityChestGenerationChance,
                ).setDefaultValue(0.25f)
                    .setMin(0f)
                    .setMax(1f)
                    .setSaveConsumer { globalConfig.ancientCityChestGenerationChance = it }
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startFloatField(
                    translate("screen.config.buriedTreasureChestGenerationChance"),
                    globalConfig.buriedTreasureChestGenerationChance,
                ).setDefaultValue(0.25f)
                    .setMin(0f)
                    .setMax(1f)
                    .setSaveConsumer { globalConfig.buriedTreasureChestGenerationChance = it }
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startFloatField(
                    translate("screen.config.endCityTreasureChestGenerationChance"),
                    globalConfig.endCityTreasureChestGenerationChance,
                ).setDefaultValue(0.25f)
                    .setMin(0f)
                    .setMax(1f)
                    .setSaveConsumer { globalConfig.endCityTreasureChestGenerationChance = it }
                    .build()
            )

            skillFruitGeneration.add(
                entryBuilder.startFloatField(
                    translate("screen.config.spawnBonusChestGenerationChance"),
                    globalConfig.spawnBonusChestGenerationChance,
                ).setDefaultValue(1f)
                    .setMin(0f)
                    .setMax(1f)
                    .setSaveConsumer { globalConfig.spawnBonusChestGenerationChance = it }
                    .build()
            )

            if (client?.world != null) {
                skillFruitGeneration.forEach { it.isRequiresRestart = true }
            }

            addEntry(skillFruitGeneration.build())

            val skillConfigEntry =
                entryBuilder.startSubCategory(translate("screen.config.skillConfig"))

            val skillConfig = globalConfig.skillConfig

            skillConfigEntry.add(
                entryBuilder.startDoubleField(
                    translate("screen.config.skillCooldownMultiplier"),
                    skillConfig.skillCooldownMultiplier ?: 1.0
                ).setDefaultValue(1.0)
                    .setMin(0.0)
                    .setSaveConsumer {
                        skillConfig.skillCooldownMultiplier = it
                    }
                    .build()
            )

            skillConfigEntry.add(
                entryBuilder.startDoubleField(
                    translate("screen.config.skillXpMultiplier"),
                    skillConfig.skillXpMultiplier ?: 1.0
                ).setDefaultValue(1.0)
                    .setMin(0.0)
                    .setSaveConsumer {
                        skillConfig.skillXpMultiplier = it
                    }
                    .build()
            )

            skillConfigEntry.add(
                entryBuilder.startStrList(
                    translate("screen.config.skillBlackList"),
                    skillConfig.skillBlackList.toList()
                ).setDefaultValue(emptyList())
                    .setSaveConsumer {
                        skillConfig.skillBlackList.clear()
                        skillConfig.skillBlackList.addAll(it)
                    }
                    .build()
            )

            addEntry(skillConfigEntry.build())
        }
    }

    private fun addGeneralCategory(
        builder: ConfigBuilder,
        entryBuilder: ConfigEntryBuilder
    ) {
        builder.getOrCreateCategory(translate("screen.config.category.general")).run {
            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.quickCastWheelHoldTime"),
                    clientConfig.quickCastWheelHoldTime
                ).setDefaultValue(250)
                    .setMin(0)
                    .setSaveConsumer { clientConfig.quickCastWheelHoldTime = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.developmentMode"),
                    clientConfig.developmentMode
                ).setDefaultValue(false)
                    .setSaveConsumer { clientConfig.developmentMode = it }
                    .build()
            )
        }
    }

    private fun addRenderCategory(
        builder: ConfigBuilder,
        entryBuilder: ConfigEntryBuilder
    ) {
        builder.getOrCreateCategory(translate("screen.config.category.render")).run {
            addEntry(
                entryBuilder.startIntField(translate("screen.config.uiOffsetX"), clientConfig.uiOffsetX)
                    .setDefaultValue(0)
                    .setSaveConsumer { clientConfig.uiOffsetX = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(translate("screen.config.uiOffsetY"), clientConfig.uiOffsetY)
                    .setDefaultValue(0)
                    .setSaveConsumer { clientConfig.uiOffsetY = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.skillSorter"),
                    SkillSorter::class.java,
                    clientConfig.skillSorter
                ).setDefaultValue(SkillSorter.DEFAULT)
                    .setEnumNameProvider { (it as SkillSorter).displayName }
                    .setSaveConsumer { clientConfig.skillSorter = it }
                    .build()
            )

            addEntry(
                entryBuilder.startStrList(translate("screen.config.layout"), clientConfig.getLayoutOfStringList())
                    .setDefaultValue(DEFAULT_LAYOUT_STRING_LIST)
                    .setCellErrorSupplier {
                        if (ClientConfig.isValidStringLayout(it)) Optional.empty()
                        else Optional.of(translate("screen.config.invalidLayout"))
                    }.setErrorSupplier {
                        val missingNumbers = ClientConfig.findMissingNumbersFromStringList(it)
                        if (missingNumbers.isEmpty()) {
                            val redundantNumbers = ClientConfig.findRedundantNumbersFromStringList(it)
                            if (redundantNumbers.isEmpty()) Optional.empty()
                            else Optional.of(
                                translate(
                                    "screen.config.redundantNumbers",
                                    redundantNumbers.joinToString(", ")
                                )
                            )
                        } else Optional.of(
                            translate(
                                "screen.config.incompleteLayout",
                                missingNumbers.joinToString(", ")
                            )
                        )
                    }.setSaveConsumer { clientConfig.setLayoutFromStringList(it) }.build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.hideSkillCrosshair"),
                    clientConfig.hideSkillCrosshair
                ).setDefaultValue(false)
                    .setSaveConsumer { clientConfig.hideSkillCrosshair = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.hideSkillInfo"),
                    clientConfig.hideSkillInfo
                ).setDefaultValue(false)
                    .setSaveConsumer { clientConfig.hideSkillInfo = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.hideSkillSlots"),
                    HideMode::class.java,
                    clientConfig.hideSkillSlots
                ).setDefaultValue(HideMode.DYNAMICALLY_HIDE)
                    .setEnumNameProvider { (it as HideMode).displayName }
                    .setSaveConsumer { clientConfig.hideSkillSlots = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.dynamicallyHideDirection"),
                    AnimationDirection::class.java,
                    clientConfig.dynamicallyHideDirection
                ).setDefaultValue(AnimationDirection.RIGHT)
                    .setEnumNameProvider { (it as AnimationDirection).displayName }
                    .setSaveConsumer { clientConfig.dynamicallyHideDirection = it }
                    .build()
            )

            addEntry(
                entryBuilder.startAlphaColorField(
                    translate("screen.config.progressBarColor"),
                    clientConfig.progressBarColor
                ).setDefaultValue(0xFFFFEE58.toInt())
                    .setAlphaMode(true)
                    .setSaveConsumer { clientConfig.progressBarColor = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.displayProgressBarBelowCrosshair"),
                    clientConfig.displayProgressBarBelowCrosshair
                ).setDefaultValue(true)
                    .setSaveConsumer { clientConfig.displayProgressBarBelowCrosshair = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.progressBarOffsetY"),
                    clientConfig.progressBarOffsetY
                ).setDefaultValue(0)
                    .setSaveConsumer { clientConfig.progressBarOffsetY = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.displaySelectedSkillSlot"),
                    clientConfig.displaySelectedSkillSlot
                ).setDefaultValue(true)
                    .setSaveConsumer { clientConfig.displaySelectedSkillSlot = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.displayQuickCastKey"),
                    clientConfig.displayQuickCastKey
                ).setDefaultValue(true)
                    .setSaveConsumer { clientConfig.displayQuickCastKey = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.useVanillaSlot"),
                    clientConfig.useVanillaSlot
                ).setDefaultValue(false)
                    .setSaveConsumer { clientConfig.useVanillaSlot = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.selectedSlotPosition"),
                    SlotPosition::class.java,
                    clientConfig.selectedSlotPosition
                ).setDefaultValue(SlotPosition.LEFT_OF_HOTBAR)
                    .setEnumNameProvider { (it as SlotPosition).displayName }
                    .setSaveConsumer { clientConfig.selectedSlotPosition = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.selectedSlotOffsetX"),
                    clientConfig.selectedSlotOffsetX
                ).setDefaultValue(0)
                    .setSaveConsumer { clientConfig.selectedSlotOffsetX = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.selectedSlotOffsetY"),
                    clientConfig.selectedSlotOffsetY
                ).setDefaultValue(0)
                    .setSaveConsumer { clientConfig.selectedSlotOffsetY = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.useRingCastingWheel"),
                    clientConfig.useRingCastingWheel
                ).setDefaultValue(true)
                    .setSaveConsumer { clientConfig.useRingCastingWheel = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.disableLearningNotifications"),
                    clientConfig.disableLearningNotifications
                ).setDefaultValue(false)
                    .setSaveConsumer { clientConfig.disableLearningNotifications = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.disableStatusEffectRenderers"),
                    clientConfig.disableStatusEffectRenderers
                ).setDefaultValue(false)
                    .setSaveConsumer { clientConfig.disableStatusEffectRenderers = it }
                    .build()
            )
        }
    }

    companion object {

        private val LOGGER = LogUtils.getLogger()
    }
}