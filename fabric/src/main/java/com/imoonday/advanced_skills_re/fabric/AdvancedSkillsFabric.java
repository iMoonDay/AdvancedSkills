package com.imoonday.advanced_skills_re.fabric;

import com.imoonday.AdvancedSkills;
import net.fabricmc.api.ModInitializer;

public final class AdvancedSkillsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AdvancedSkills.INSTANCE.init();
    }
}
