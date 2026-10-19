package me.abdiskiosk.lectiocalendar.calendar;

import me.abdiskiosk.lectiocalendar.db.object.LectioCalendarEvent;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EventReminders {
    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Europe/Copenhagen");

    private EventReminders() {}

    public static Map<LectioCalendarEvent, Integer> minutesBefore(Collection<LectioCalendarEvent> events) {
        Map<LectioCalendarEvent, Integer> reminders = new HashMap<>();
        Set<LocalDate> daysWithReminder = new HashSet<>();
        events.stream().sorted(Comparator.comparing(LectioCalendarEvent::getStart)
                        .thenComparingInt(LectioCalendarEvent::getId))
                .filter(event -> !"CANCELLED".equalsIgnoreCase(event.getState()))
                .forEach(event -> {
                    LocalDate day = event.getStart().toInstant().atZone(SCHEDULE_ZONE).toLocalDate();
                    reminders.put(event, daysWithReminder.add(day) ? 30 : 5);
                });
        return reminders;
    }
}
