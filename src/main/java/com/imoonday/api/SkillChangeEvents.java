package com.imoonday.api;

import com.imoonday.utils.Skill;
import com.imoonday.utils.SkillSlot;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SkillChangeEvents {

    public static final Event<OnEquipped> EQUIPPED = EventFactory.createArrayBacked(OnEquipped.class,
            (listeners -> (player, slot, skill) -> {
                for (OnEquipped listener : listeners) {
                    if (!listener.onEquipped(player, slot, skill)) {
                        return false;
                    }
                }
                return true;
            }));

    public static final Event<OnUnequipped> UNEQUIPPED = EventFactory.createArrayBacked(OnUnequipped.class,
            (listeners -> (player, slot, skill) -> {
                for (OnUnequipped listener : listeners) {
                    if (!listener.onUnequipped(player, slot, skill)) {
                        return false;
                    }
                }
                return true;
            }));

    public static final Event<PostEquipped> POST_EQUIPPED = EventFactory.createArrayBacked(PostEquipped.class,
            (listeners -> (player, slot, skill) -> {
                for (PostEquipped listener : listeners) {
                    listener.postEquipped(player, slot, skill);
                }
            }));

    public static final Event<PostUnequipped> POST_UNEQUIPPED = EventFactory.createArrayBacked(PostUnequipped.class,
            (listeners -> (player, slot, skill) -> {
                for (PostUnequipped listener : listeners) {
                    listener.postUnequipped(player, slot, skill);
                }
            }));

    @FunctionalInterface
    public interface OnEquipped {
        boolean onEquipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }

    @FunctionalInterface
    public interface OnUnequipped {
        boolean onUnequipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }

    @FunctionalInterface
    public interface PostEquipped {
        void postEquipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }

    @FunctionalInterface
    public interface PostUnequipped {
        void postUnequipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }
}
