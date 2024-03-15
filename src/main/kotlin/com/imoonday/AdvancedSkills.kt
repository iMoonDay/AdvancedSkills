package com.imoonday

import com.imoonday.config.Config
import com.imoonday.custom.*
import com.imoonday.init.*
import com.imoonday.skill.Skill
import com.imoonday.util.*
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val MOD_ID = "advanced_skills"
val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

object AdvancedSkills : ModInitializer {

    override fun onInitialize() {
        Config.load()
        customSkillTest()
        ModSkills.init()
        ModChannels.register()
        ModCommands.init()
        SkillArgumentType.register()
        ModItemGroups.init()
        ModItems.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        ModGameRules.init()
        EventHandler.register()
    }

    private fun customSkillTest() {
        CustomSkill(
            id = id("test"),
            nameKey = "advanced_skills.skill.empty",
            rarity = Skill.Rarity.EPIC,
            event = Event(
                listOf(
                    JumpAction(), MessageAction(mapOf("value" to "jump", "overlay" to "false")),
                    EquippedCondition(
                        mapOf(),
                        ActionGroup(listOf()),
                    ),
                    ReturnImpl(UseResult.success()),
                ),
                listOf(TriggerImpl("test"))
            ),
            types = listOf(SkillType.MOVEMENT),
            cooldown = 20,
        ).run {
            register()
            CustomSkillHandler.save(this)
        }
    }
}