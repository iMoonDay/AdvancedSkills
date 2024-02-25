package com.imoonday

import com.imoonday.api.SkillChangeEvents
import com.imoonday.components.equippedSkills
import com.imoonday.components.isCooling
import com.imoonday.components.stopUsingSkill
import com.imoonday.init.*
import com.imoonday.network.Channels
import com.imoonday.skills.Skills
import com.imoonday.trigger.DeathTrigger
import com.imoonday.trigger.RespawnTrigger
import com.imoonday.trigger.SkillTriggerHandler
import com.imoonday.utils.SkillArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import org.slf4j.LoggerFactory

const val MOD_ID = "advanced_skills"

object AdvancedSkills : ModInitializer {
    private val logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        Skills.init()
        Channels.registerServer()
        ModCommands.init()
        SkillArgumentType.register()
        ModItemGroups.init()
        ModItems.init()
        ModEffects.init()
        ModSounds.init()
        ModEntities.init()
        registerEvents()
    }

    private fun registerEvents() {
        SkillChangeEvents.EQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.onEquipped(player, slot, skill)
        }
        SkillChangeEvents.UNEQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.onUnequipped(player, slot, skill)
        }
        SkillChangeEvents.POST_EQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.postEquipped(player, slot, skill)
            SkillTriggerHandler.onStart(player, skill)
        }
        SkillChangeEvents.POST_UNEQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.postUnequipped(player, slot, skill)
            player.stopUsingSkill(skill)
        }
        ServerPlayerEvents.AFTER_RESPAWN.register { oldPlayer, newPlayer, alive ->
            newPlayer.equippedSkills
                .filterIsInstance<RespawnTrigger>()
                .forEach { it.afterRespawn(oldPlayer, newPlayer, alive) }
        }
        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, source, amount ->
            (entity as? ServerPlayerEntity)?.run {
                equippedSkills
                    .filterNot { isCooling(it) }
                    .filterIsInstance<DeathTrigger>()
                    .all { it.allowDeath(this, source, amount) }
            } ?: true
        }
        AttackEntityCallback.EVENT.register { player, _, _, _, _ ->
            if (player.isDisarmed()) ActionResult.FAIL else ActionResult.PASS
        }
        UseItemCallback.EVENT.register { player, _, hand ->
            val stack = player.getStackInHand(hand)
            if (player.isDisarmed()) {
                TypedActionResult.fail(stack)
            } else TypedActionResult.pass(stack)
        }
    }

    fun id(name: String): Identifier = Identifier(MOD_ID, name)

    fun itemPath(name: String): Identifier = id("textures/item/$name.png")
}