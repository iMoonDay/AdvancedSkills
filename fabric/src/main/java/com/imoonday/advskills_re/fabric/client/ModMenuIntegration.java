package com.imoonday.advskills_re.fabric.client;

import com.imoonday.advskills_re.client.AdvancedSkillsClient;
import com.imoonday.advskills_re.client.screen.ConfigScreenHandler;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AdvancedSkillsClient.clothConfigLoaded ? ConfigScreenHandler.createScreen(parent) : null;
    }
}
