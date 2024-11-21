package com.imoonday.advanced_skills_re.api;

import com.imoonday.component.EntityPropertyComponent;

public interface Propertied {

    default EntityPropertyComponent getPropertyComponent() {
        return null;
    }
}