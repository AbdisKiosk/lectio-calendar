package me.abdiskiosk.lectiocalendar.db.dao;

import me.abdiskiosk.lectiocalendar.db.DB;
import me.abdiskiosk.lectiocalendar.db.object.LectioCalendarEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class LectioCalendarEventDAO {

    private final DB db;

    public LectioCalendarEventDAO(@NotNull DB db) {
        this.db = db;
    }

    public void insert(@Nullable String title, @Nullable String team, @Nullable String teachers, @Nullable String room,
                       @Nullable String state, int queriedWeekNum, @NotNull Date start, @NotNull Date end)
            throws SQLException {
        db.update(
                """
                INSERT INTO lectio_calendar_events
                (title, team, teachers, room, state, queried_week_num, start, end, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                title, team, teachers, room, state, queriedWeekNum, new java.sql.Timestamp(start.getTime()), new java.sql.Timestamp(end.getTime())
        );
    }

    public @Nullable Date getOldest() throws SQLException {
        return db.query(
                """
                        SELECT created_at
                        FROM lectio_calendar_events
                        ORDER BY created_at DESC
                        """,
                row -> row.getTimestamp(1)
        ).stream().findFirst().orElse(null);
    }


    public @Nullable Date getOldest(int weekNum) throws SQLException {
        return db.query(
                """
                        SELECT created_at
                        FROM lectio_calendar_events
                        WHERE queried_week_num = ? ORDER BY created_at DESC
                        """,
                row -> row.getTimestamp(1),
                weekNum
        ).stream().findFirst().orElse(null);
    }

    public @NotNull List<LectioCalendarEvent> queryEvents() throws SQLException {
        return db.query(
                """
                        SELECT id, title, team, teachers, room, state, queried_week_num, start, end, created_at
                        FROM lectio_calendar_events
                        """,
                row -> new LectioCalendarEvent(
                        row.getInt(1),
                        row.getString(2),
                        row.getString(3),
                        row.getString(4),
                        row.getString(5),
                        row.getString(6),
                        row.getInt(7),
                        row.getTimestamp(8),
                        row.getTimestamp(9),
                        row.getTimestamp(10)
                )
        );
    }

    public long removeWeek(int weekNum) throws SQLException {
        return db.update("DELETE FROM lectio_calendar_events WHERE queried_week_num = ?", weekNum);
    }

    public void removeAll() throws SQLException {
        db.update("DELETE FROM lectio_calendar_events");
    }

}
