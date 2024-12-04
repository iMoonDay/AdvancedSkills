package com.imoonday.advskills_re.client.screen

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.client.ClientConfig.Companion.DEFAULT_LAYOUT_STRING_LIST
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
            val builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(translate("screen.config.title"))
                .setSavingRunnable { ClientConfig.get().save() }

            val config = ClientConfig.get()

            val render = builder.getOrCreateCategory(translate("screen.config.category.render"))

            val entryBuilder = builder.entryBuilder()

            render.run {
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
            }

            val general = builder.getOrCreateCategory(translate("screen.config.category.general"))

            general.run {
                addEntry(
                    entryBuilder.startIntField(
                        translate("screen.config.quickCastWheelHoldTime"),
                        config.quickCastWheelHoldTime
                    ).setDefaultValue(250)
                        .setMin(0)
                        .setSaveConsumer { config.quickCastWheelHoldTime = it }
                        .build()
                )
            }

            return builder.build()
        } catch (e: Exception) {
            LOGGER.error("Error while creating config screen", e)
            return parent
        }
    }
}