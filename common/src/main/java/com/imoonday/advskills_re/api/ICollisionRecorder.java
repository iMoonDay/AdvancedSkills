package com.imoonday.advskills_re.api;

public interface ICollisionRecorder {

    default boolean wasHorizontalCollision() {
        return false;
    }

    default boolean wasVerticalCollision() {
        return false;
    }

    default boolean wasGroundCollision() {
        return false;
    }
}
