package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.api.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.s2c.*
import com.imoonday.advskills_re.trigger.*
import dev.architectury.event.*
import dev.architectury.event.events.common.*
import net.minecraft.block.*
import net.minecraft.loot.*
import net.minecraft.loot.condition.*
import net.minecraft.loot.entry.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

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
        PlayerEvent.PLAYER_CLONE.register { oldPlayer, newPlayer, _ ->
            newPlayer.copyDataFrom(oldPlayer)
            newPlayer.properties.copyFrom(oldPlayer.properties)
            newPlayer.syncData(false)
            newPlayer.syncProperties()
        }
        AllowDeathEvent.EVENT.register { player, source, amount ->
            player.getTriggers<DeathTrigger>()
                .all { it.allowDeath(player, source, amount) }
        }
        EntityEvent.LIVING_DEATH.register { entity, source ->
            if (entity is ServerPlayerEntity) {
                entity.getTriggers<DeathTrigger>()
                    .forEach { it.onDeath(entity, source) }
            }
            EventResult.pass()
        }
        PlayerEvent.PLAYER_RESPAWN.register { player, _ ->
            player.usingSkills.forEach { player.stopUsing(it) }
            player.getTriggers<RespawnTrigger>()
                .forEach { it.afterRespawn(player) }
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
        val lootTables = mapOf(
            Blocks.OAK_LEAVES.lootTableId to 0.005f,
            Blocks.DARK_OAK_LEAVES.lootTableId to 0.005f,
            LootTables.FISHING_TREASURE_GAMEPLAY to 0.1f,
            LootTables.ANCIENT_CITY_CHEST to 0.25f,
            LootTables.BURIED_TREASURE_CHEST to 0.25f,
            LootTables.END_CITY_TREASURE_CHEST to 0.25f
        )
        val pool = {
            LootPool.builder()
                .with(ItemEntry.builder(ModItems.COMMON_SKILL_FRUIT.get()).weight(256))
                .with(ItemEntry.builder(ModItems.UNCOMMON_SKILL_FRUIT.get()).weight(128))
                .with(ItemEntry.builder(ModItems.RARE_SKILL_FRUIT.get()).weight(32))
                .with(ItemEntry.builder(ModItems.SUPERB_SKILL_FRUIT.get()).weight(16))
                .with(ItemEntry.builder(ModItems.EPIC_SKILL_FRUIT.get()).weight(8))
                .with(ItemEntry.builder(ModItems.LEGENDARY_SKILL_FRUIT.get()).weight(4))
                .with(ItemEntry.builder(ModItems.MYTHIC_SKILL_FRUIT.get()).weight(2))
                .with(ItemEntry.builder(ModItems.UNIQUE_SKILL_FRUIT.get()).weight(1))
        }
        LootEvent.MODIFY_LOOT_TABLE.register { _, identifier, context, builtin ->
            if (identifier in lootTables.keys && builtin) {
                context.addPool(
                    pool().conditionally(RandomChanceLootCondition.builder(lootTables[identifier]!!))
                        .build()
                )
            }
        }
        PlayerEvent.PLAYER_JOIN.register {
            Channels.SYNC_CONFIG_S2C.sendToPlayer(it, SyncConfigS2CPacket(it.server.skillConfig.toTag(NbtCompound())))
        }
    }
}