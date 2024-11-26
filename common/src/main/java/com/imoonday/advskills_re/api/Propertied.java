package com.imoonday.advskills_re.api;

import com.imoonday.advskills_re.component.EntityPropertyComponent;

public interface Propertied {

    default EntityPropertyComponent getPropertyComponent() {
        return null;
    }
}