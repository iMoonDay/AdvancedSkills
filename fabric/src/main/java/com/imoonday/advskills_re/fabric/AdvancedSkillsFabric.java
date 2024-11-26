package com.imoonday.advskills_re.fabric;

import com.imoonday.advskills_re.AdvancedSkills;
import net.fabricmc.api.ModInitializer;

public final class AdvancedSkillsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AdvancedSkills.init();
    }
}
