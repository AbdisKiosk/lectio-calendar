package me.abdiskiosk.lectiocalendar.db.object;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Data
public class LectioCalendarEvent {

    private final int id;
    private final @Nullable String title;
    private final @Nullable String team;
    private final @Nullable String teachers;
    private final @Nullable String room;
    private final @Nullable String state;
    private final int queriedWeekNum;
    private final @NotNull Date start;
    private final @NotNull Date end;
    private final @NotNull Date createdAt;

}
