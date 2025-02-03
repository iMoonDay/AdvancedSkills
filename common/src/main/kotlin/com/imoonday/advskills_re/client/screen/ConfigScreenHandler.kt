package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.ClientConfig.Companion.DEFAULT_LAYOUT_STRING_LIST
import com.imoonday.advskills_re.client.render.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import com.mojang.logging.*
import me.shedaniel.clothconfig2.api.*
import net.minecraft.client.gui.screen.*
import java.util.*

object ConfigScreenHandler {

    private val LOGGER = LogUtils.getLogger()

    @JvmStatic
    fun createScreen(parent: Screen?): Screen? {
        if (!AdvancedSkillsClient.clothConfigLoaded) return parent
        try {
            val config = ClientConfig.get()
            val globalConfig = GlobalConfig.get()

            val builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(translate("screen.config.title"))
                .setSavingRunnable(::save)

            val entryBuilder = builder.entryBuilder()

            addRenderCategory(builder, entryBuilder, config)
            addGeneralCategory(builder, entryBuilder, config)
            addGlobalCategory(builder, entryBuilder, globalConfig)

            return builder.build()
        } catch (e: Exception) {
            LOGGER.error("Error while creating config screen", e)
            return parent
        }
    }

    private fun save() {
        ClientConfig.get().save()
        val globalConfig = GlobalConfig.get()
        globalConfig.save()

        val client = client
        if (client?.isIntegratedServerRunning == true) {
            client.server?.let {
                Channels.SYNC_CONFIG_S2C.sendToPlayers(
                    it.playerManager.playerList,
                    SyncConfigS2CPacket(globalConfig.toNbt(), SyncConfigS2CPacket.ConfigType.GLOBAL)
                )
            }
        }
    }

    private fun addGlobalCategory(
        builder: ConfigBuilder,
        entryBuilder: ConfigEntryBuilder,
        globalConfig: GlobalConfig
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
        entryBuilder: ConfigEntryBuilder,
        config: ClientConfig
    ) {
        builder.getOrCreateCategory(translate("screen.config.category.general")).run {
            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.quickCastWheelHoldTime"),
                    config.quickCastWheelHoldTime
                ).setDefaultValue(250)
                    .setMin(0)
                    .setSaveConsumer { config.quickCastWheelHoldTime = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.developmentMode"),
                    config.developmentMode
                ).setDefaultValue(false)
                    .setSaveConsumer { config.developmentMode = it }
                    .build()
            )
        }
    }

    private fun addRenderCategory(
        builder: ConfigBuilder,
        entryBuilder: ConfigEntryBuilder,
        config: ClientConfig
    ) {
        builder.getOrCreateCategory(translate("screen.config.category.render")).run {
            addEntry(
                entryBuilder.startIntField(translate("screen.config.uiOffsetX"), config.uiOffsetX)
                    .setDefaultValue(0)
                    .setSaveConsumer { config.uiOffsetX = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(translate("screen.config.uiOffsetY"), config.uiOffsetY)
                    .setDefaultValue(0)
                    .setSaveConsumer { config.uiOffsetY = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.skillSorter"),
                    SkillSorter::class.java,
                    config.skillSorter
                ).setDefaultValue(SkillSorter.DEFAULT)
                    .setEnumNameProvider { (it as SkillSorter).displayName }
                    .setSaveConsumer { config.skillSorter = it }
                    .build()
            )

            addEntry(
                entryBuilder.startStrList(translate("screen.config.layout"), config.getLayoutOfStringList())
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
                    }.setSaveConsumer { config.setLayoutFromStringList(it) }.build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.hideSkillCrosshair"),
                    config.hideSkillCrosshair
                ).setDefaultValue(false)
                    .setSaveConsumer { config.hideSkillCrosshair = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.hideSkillInfo"),
                    config.hideSkillInfo
                ).setDefaultValue(false)
                    .setSaveConsumer { config.hideSkillInfo = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.hideSkillSlots"),
                    HideMode::class.java,
                    config.hideSkillSlots
                ).setDefaultValue(HideMode.DYNAMICALLY_HIDE)
                    .setEnumNameProvider { (it as HideMode).displayName }
                    .setSaveConsumer { config.hideSkillSlots = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.dynamicallyHideDirection"),
                    AnimationDirection::class.java,
                    config.dynamicallyHideDirection
                ).setDefaultValue(AnimationDirection.RIGHT)
                    .setEnumNameProvider { (it as AnimationDirection).displayName }
                    .setSaveConsumer { config.dynamicallyHideDirection = it }
                    .build()
            )

            addEntry(
                entryBuilder.startAlphaColorField(
                    translate("screen.config.progressBarColor"),
                    config.progressBarColor
                ).setDefaultValue(0xFFFFEE58.toInt())
                    .setAlphaMode(true)
                    .setSaveConsumer { config.progressBarColor = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.displayProgressBarBelowCrosshair"),
                    config.displayProgressBarBelowCrosshair
                ).setDefaultValue(true)
                    .setSaveConsumer { config.displayProgressBarBelowCrosshair = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.progressBarOffsetY"),
                    config.progressBarOffsetY
                ).setDefaultValue(0)
                    .setSaveConsumer { config.progressBarOffsetY = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.displaySelectedSkillSlot"),
                    config.displaySelectedSkillSlot
                ).setDefaultValue(true)
                    .setSaveConsumer { config.displaySelectedSkillSlot = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.displayQuickCastKey"),
                    config.displayQuickCastKey
                ).setDefaultValue(true)
                    .setSaveConsumer { config.displayQuickCastKey = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.useVanillaSlot"),
                    config.useVanillaSlot
                ).setDefaultValue(false)
                    .setSaveConsumer { config.useVanillaSlot = it }
                    .build()
            )

            addEntry(
                entryBuilder.startEnumSelector(
                    translate("screen.config.selectedSlotPosition"),
                    SlotPosition::class.java,
                    config.selectedSlotPosition
                ).setDefaultValue(SlotPosition.LEFT_OF_HOTBAR)
                    .setEnumNameProvider { (it as SlotPosition).displayName }
                    .setSaveConsumer { config.selectedSlotPosition = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.selectedSlotOffsetX"),
                    config.selectedSlotOffsetX
                ).setDefaultValue(0)
                    .setSaveConsumer { config.selectedSlotOffsetX = it }
                    .build()
            )

            addEntry(
                entryBuilder.startIntField(
                    translate("screen.config.selectedSlotOffsetY"),
                    config.selectedSlotOffsetY
                ).setDefaultValue(0)
                    .setSaveConsumer { config.selectedSlotOffsetY = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.useRingCastingWheel"),
                    config.useRingCastingWheel
                ).setDefaultValue(true)
                    .setSaveConsumer { config.useRingCastingWheel = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.disableLearningNotifications"),
                    config.disableLearningNotifications
                ).setDefaultValue(false)
                    .setSaveConsumer { config.disableLearningNotifications = it }
                    .build()
            )

            addEntry(
                entryBuilder.startBooleanToggle(
                    translate("screen.config.disableStatusEffectRenderers"),
                    config.disableStatusEffectRenderers
                ).setDefaultValue(false)
                    .setSaveConsumer { config.disableStatusEffectRenderers = it }
                    .build()
            )
        }
    }
}