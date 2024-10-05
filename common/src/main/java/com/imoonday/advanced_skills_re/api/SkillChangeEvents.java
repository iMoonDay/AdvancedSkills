package com.imoonday.advanced_skills_re.api;

import com.imoonday.skill.Skill;
import com.imoonday.util.SkillSlot;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SkillChangeEvents {

    Event<OnEquipped> EQUIPPED = EventFactory.createEventResult();
    Event<OnUnequipped> UNEQUIPPED = EventFactory.createEventResult();
    Event<PostEquipped> POST_EQUIPPED = EventFactory.createLoop();
    Event<PostUnequipped> POST_UNEQUIPPED = EventFactory.createLoop();

    interface OnEquipped {

        EventResult onEquipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }

    interface OnUnequipped {

        EventResult onUnequipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }

    interface PostEquipped {

        void postEquipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }

    interface PostUnequipped {

        void postUnequipped(ServerPlayerEntity player, SkillSlot slot, Skill skill);
    }
}
