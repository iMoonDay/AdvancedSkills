package com.imoonday.advskills_re.api;

import com.imoonday.advskills_re.component.PlayerDataComponent;

public interface PlayerDataContainer {

    default PlayerDataComponent getDataComponent() {
        return null;
    }
}
