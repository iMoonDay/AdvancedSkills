package com.imoonday.advskills_re.api;

import net.minecraft.entity.Entity;

import java.util.function.Predicate;

public interface DamageFilter {

    default boolean passDamage(Entity entity) {
        return false;
    }

    default void setNoDamagePredicate(Predicate<Entity> predicate) {

    }
}
