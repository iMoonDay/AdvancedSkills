package com.imoonday.advanced_skills_re.api;

import com.imoonday.component.PlayerDataComponent;

public interface PlayerDataContainer {

    default PlayerDataComponent getDataComponent() {
        return null;
    }
}
