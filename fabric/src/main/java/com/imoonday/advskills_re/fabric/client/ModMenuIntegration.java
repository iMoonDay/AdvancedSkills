package com.imoonday.advskills_re.fabric.client;

import com.imoonday.advskills_re.client.AdvancedSkillsClient;
import com.imoonday.advskills_re.client.screen.ModConfigScreenFactory;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AdvancedSkillsClient.clothConfigLoaded ? new ModConfigScreenFactory().createScreen(parent) : null;
    }
}
