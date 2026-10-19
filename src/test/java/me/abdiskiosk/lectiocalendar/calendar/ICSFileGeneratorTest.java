package me.abdiskiosk.lectiocalendar.calendar;

import me.abdiskiosk.lectiocalendar.db.object.LectioCalendarEvent;
import net.fortuna.ical4j.data.CalendarBuilder;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ICSFileGeneratorTest {
    private final ICSFileGenerator generator = new ICSFileGenerator();

    @Test
    void assignsRemindersChronologicallyAndResetsEachDay() throws Exception {
        var later = event(2, "Later", "2026-10-19T17:00+02:00", null);
        var nextDay = event(3, "Next day", "2026-10-20T08:00+02:00", null);
        var first = event(1, "Afternoon first", "2026-10-19T15:00+02:00", null);
        var events = List.of(later, nextDay, first);

        String ics = generateAndValidate(events);

        assertReminder(ics, "Afternoon first", 30);
        assertReminder(ics, "Later", 5);
        assertReminder(ics, "Next day", 30);
        assertEquals(List.of(later, nextDay, first), events);
    }

    @Test
    void groupsByCopenhagenDateAcrossUtcMidnightAndDaylightSavingChanges() throws Exception {
        for (String offset : List.of("+02:00", "+01:00")) {
            String date = offset.equals("+02:00") ? "2026-10-24" : "2026-10-26";
            String ics = generateAndValidate(List.of(
                    event(2, "Morning", date + "T08:00" + offset, null),
                    event(1, "After midnight", date + "T00:15" + offset, null)));

            assertReminder(ics, "After midnight", 30);
            assertReminder(ics, "Morning", 5);
        }
    }

    @Test
    void cancelledEventsDoNotGetRemindersOrTakeTheFirstSlot() throws Exception {
        String ics = generateAndValidate(List.of(
                event(1, "Cancelled", "2026-10-19T08:00+02:00", "CANCELLED"),
                event(2, "First active", "2026-10-19T10:00+02:00", null)));

        assertFalse(eventBlock(ics, "Cancelled").contains("BEGIN:VALARM"));
        assertReminder(ics, "First active", 30);
    }

    @Test
    void simultaneousEventsHaveOneDeterministicFirstReminder() throws Exception {
        var first = event(1, "First", "2026-10-19T08:00+02:00", null);
        var second = event(2, "Second", "2026-10-19T08:00+02:00", null);
        for (var events : List.of(List.of(second, first), List.of(first, second))) {
            String ics = generateAndValidate(events);
            assertReminder(ics, "First", 30);
            assertReminder(ics, "Second", 5);
        }
    }

    @Test
    void emptyCalendarIsValidAndContainsNoAlarms() throws Exception {
        assertFalse(generateAndValidate(List.of()).contains("BEGIN:VALARM"));
    }

    private String generateAndValidate(List<LectioCalendarEvent> events) throws Exception {
        String ics = generator.generateIcs(events);
        new CalendarBuilder().build(new StringReader(ics)).validate();
        return ics;
    }

    private static void assertReminder(String ics, String title, int minutes) {
        String block = eventBlock(ics, title);
        assertEquals(1, block.split("BEGIN:VALARM", -1).length - 1);
        assertTrue(block.contains("TRIGGER:-PT" + minutes + "M\r\n"), block);
        assertTrue(block.contains("ACTION:DISPLAY\r\n"), block);
        assertTrue(block.contains("DESCRIPTION:Team - " + title + "\r\n"), block);
    }

    private static String eventBlock(String ics, String title) {
        return java.util.Arrays.stream(ics.split("BEGIN:VEVENT\r\n"))
                .filter(block -> block.contains("SUMMARY:Team - " + title + "\r\n"))
                .findFirst().orElseThrow().split("END:VEVENT")[0];
    }

    private static LectioCalendarEvent event(int id, String title, String start, String state) {
        var instant = ZonedDateTime.parse(start).toInstant();
        return new LectioCalendarEvent(id, title, "Team", "Teacher", "Room", state, 43,
                Date.from(instant), Date.from(instant.plus(Duration.ofHours(1))), Date.from(instant));
    }
}
