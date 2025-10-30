package me.abdiskiosk.lectiocalendar.task;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskSchedulingManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public void executeAndSchedule(int repeatSeconds, @NotNull Runnable task) {
        scheduler.scheduleAtFixedRate(task, 0, repeatSeconds, TimeUnit.SECONDS);
    }

}
