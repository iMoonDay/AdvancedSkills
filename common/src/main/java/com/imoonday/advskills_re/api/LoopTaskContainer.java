package com.imoonday.advskills_re.api;

import com.imoonday.advskills_re.util.LoopTask;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LoopTaskContainer {

    default int addTask(LoopTask loopTask) {
        return -1;
    }

    default void removeTask(int id) {

    }

    @Nullable
    default LoopTask getTask(int id) {
        return null;
    }

    default void clearTasks() {

    }

    default List<LoopTask> getTasks() {
        return List.of();
    }

    default void tickTasks() {

    }
}
