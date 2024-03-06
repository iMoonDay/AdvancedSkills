package com.imoonday.util

import com.imoonday.api.SkillChangeEvents
import com.imoonday.component.equippedSkills
import com.imoonday.component.isCooling
import com.imoonday.component.stopUsingSkill
import com.imoonday.config.Config
import com.imoonday.entity.render.feature.IceLayer
import com.imoonday.entity.render.feature.SkillLayer
import com.imoonday.entity.render.feature.StatusEffectLayer
import com.imoonday.init.ModItems
import com.imoonday.init.ModSkills
import com.imoonday.init.isDisarmed
import com.imoonday.network.SyncConfigS2CPacket
import com.imoonday.render.SkillSlotRenderer
import com.imoonday.trigger.DeathTrigger
import com.imoonday.trigger.FeatureRendererTrigger
import com.imoonday.trigger.RespawnTrigger
import com.imoonday.trigger.SkillTriggerHandler
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.PlayerEntityRenderer
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTables
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.TypedActionResult

object EventHandler {

    fun register() {
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
            if (player.isDisarmed) ActionResult.FAIL else ActionResult.PASS
        }
        UseItemCallback.EVENT.register { player, _, hand ->
            val stack = player.getStackInHand(hand)
            if (player.isDisarmed) {
                TypedActionResult.fail(stack)
            } else TypedActionResult.pass(stack)
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
            .with(ItemEntry.builder(ModItems.COMMON_SKILL_FRUIT).weight(128))
            .with(ItemEntry.builder(ModItems.UNCOMMON_SKILL_FRUIT).weight(64))
            .with(ItemEntry.builder(ModItems.RARE_SKILL_FRUIT).weight(32))
            .with(ItemEntry.builder(ModItems.SUPERB_SKILL_FRUIT).weight(16))
            .with(ItemEntry.builder(ModItems.EPIC_SKILL_FRUIT).weight(8))
            .with(ItemEntry.builder(ModItems.LEGENDARY_SKILL_FRUIT).weight(4))
            .with(ItemEntry.builder(ModItems.MYTHIC_SKILL_FRUIT).weight(2))
            .with(ItemEntry.builder(ModItems.UNIQUE_SKILL_FRUIT).weight(1))
            .conditionally(RandomChanceLootCondition.builder(0.005f))
        LootTableEvents.MODIFY.register { _, _, identifier, builder, source ->
            if (identifier in lootTables && source.isBuiltin) {
                builder.pool(pool.build())
            }
        }
        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            sender.sendPacket(SyncConfigS2CPacket(Config.instance.toTag(NbtCompound())))
        }
    }

    fun registerClient() {
        HudRenderCallback.EVENT.register { context, delta ->
            val client = MinecraftClient.getInstance()
            SkillSlotRenderer.render(client, context, delta)
        }
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register { entityType, renderer, helper, context ->
            helper.register(StatusEffectLayer(renderer, context))
            helper.register(IceLayer(renderer, context))
            if (renderer is PlayerEntityRenderer) ModSkills.SKILLS.filterIsInstance<FeatureRendererTrigger>()
                .forEach { helper.register(SkillLayer(renderer, context, it)) }
        }
    }
}