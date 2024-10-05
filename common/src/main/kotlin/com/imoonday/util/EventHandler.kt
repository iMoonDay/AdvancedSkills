package com.imoonday.util

import com.imoonday.advanced_skills_re.api.*
import com.imoonday.config.*
import com.imoonday.entity.render.feature.*
import com.imoonday.init.*
import com.imoonday.network.*
import com.imoonday.render.*
import com.imoonday.skill.*
import com.imoonday.trigger.*
import dev.architectury.event.*
import dev.architectury.event.events.client.*
import dev.architectury.event.events.client.ClientTooltipEvent.Render
import dev.architectury.event.events.common.*
import net.minecraft.block.*
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.model.*
import net.minecraft.loot.*
import net.minecraft.loot.condition.*
import net.minecraft.loot.entry.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.util.*

object EventHandler {

    fun register() {
        SkillChangeEvents.EQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.onEquipped(player, slot, skill).toEventResult()
        }
        SkillChangeEvents.UNEQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.onUnequipped(player, slot, skill).toEventResult()
        }
        SkillChangeEvents.POST_EQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.postEquipped(player, slot, skill)
            SkillTriggerHandler.onStart(player, skill)
        }
        SkillChangeEvents.POST_UNEQUIPPED.register { player, slot, skill ->
            SkillTriggerHandler.postUnequipped(player, slot, skill)
            player.stopUsing(skill)
        }
        PlayerEvent.PLAYER_CLONE.register { oldPlayer, newPlayer, alive ->
            newPlayer.usingSkills.forEach { newPlayer.stopUsing(it) }
            newPlayer.getTriggers<RespawnTrigger>()
                .forEach { it.afterRespawn(oldPlayer, newPlayer, alive) }
        }
        AllowDeathEvent.EVENT.register { entity, source, amount ->
            entity.run {
                getTriggers<DeathTrigger>()
                    .filterNot { isCooling(it.getAsSkill()) }
                    .all { it.allowDeath(this, source, amount) }
            }.toEventResult()
        }
        PlayerEvent.ATTACK_ENTITY.register { player, _, _, _, _ ->
            if (player.isDisarmed) EventResult.interruptFalse()
            else EventResult.pass()
        }
        InteractionEvent.RIGHT_CLICK_ITEM.register { player, hand ->
            val stack = player.getStackInHand(hand)
            if (player.isDisarmed) CompoundEventResult.interruptFalse(stack)
            else CompoundEventResult.pass()
        }
        val lootTables = listOf(
            Blocks.OAK_LEAVES.lootTableId,
            Blocks.DARK_OAK_LEAVES.lootTableId,
            LootTables.FISHING_TREASURE_GAMEPLAY,
            LootTables.ANCIENT_CITY_CHEST,
            LootTables.BURIED_TREASURE_CHEST,
            LootTables.END_CITY_TREASURE_CHEST
        )
        val pool = LootPool.builder()
            .with(ItemEntry.builder(ModItems.COMMON_SKILL_FRUIT).weight(256))
            .with(ItemEntry.builder(ModItems.UNCOMMON_SKILL_FRUIT).weight(128))
            .with(ItemEntry.builder(ModItems.RARE_SKILL_FRUIT).weight(32))
            .with(ItemEntry.builder(ModItems.SUPERB_SKILL_FRUIT).weight(16))
            .with(ItemEntry.builder(ModItems.EPIC_SKILL_FRUIT).weight(8))
            .with(ItemEntry.builder(ModItems.LEGENDARY_SKILL_FRUIT).weight(4))
            .with(ItemEntry.builder(ModItems.MYTHIC_SKILL_FRUIT).weight(2))
            .with(ItemEntry.builder(ModItems.UNIQUE_SKILL_FRUIT).weight(1))
            .conditionally(RandomChanceLootCondition.builder(0.005f))
        LootEvent.MODIFY_LOOT_TABLE.register { _, identifier, context, builtin ->
            if (identifier in lootTables && builtin) {
                context.addPool(pool.build())
            }
        }
        PlayerEvent.PLAYER_JOIN.register {
            it.sendPacket(SyncConfigS2CPacket(Config.instance.toTag(NbtCompound())))
        }
        LifecycleEvent.SERVER_STARTED.register {
            Config.initWatchService(it)
        }
    }

    fun registerClient() {
        ClientGuiEvent.RENDER_HUD.register { context, _ ->
            SkillSlotRenderer.render(client!!, context)
            Skill.getTriggers<HudRenderTrigger>().forEach { it.render(context) }
            Skill.getTriggers<CrosshairTrigger> { it.shouldRender() && it.getPriority() < 0 }
                .minByOrNull { it.getPriority() }
                ?.render(context)
            Skill.getTriggers<CrosshairTrigger> { it.shouldRender() && it.getPriority() >= 0 }
                .maxByOrNull { it.getPriority() }
                ?.render(context)
        }
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register { _, renderer, helper, context ->
            helper.register(StatusEffectLayer(renderer, context))
            helper.register(IceLayer(renderer, context))
            if (renderer is PlayerEntityRenderer) Skill.getTriggers<FeatureRendererTrigger>()
                .forEach { helper.register(SkillLayer(renderer, context, it)) }
            if (renderer is LivingEntityRenderer) Skill.getTriggers<TargetRenderTrigger>()
                .forEach { helper.register(TargetLayer(renderer, context, it)) }
        }
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            Skill.getTriggers<WorldRendererTrigger>().forEach { it.renderAfterEntities(context) }
        }
        WorldRenderEvents.LAST.register { context ->
            Skill.getTriggers<WorldRendererTrigger>().forEach { it.renderLast(context) }
        }
        ModelLoadingPlugin.register {
            it.addModels(TargetRenderTrigger.modelId)
        }
    }
}