package me.abdiskiosk.lectiocalendar.lectio;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import dk.zentoc.LectioSession;
import me.abdiskiosk.lectiocalendar.calendar.LectioCalendarEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class LectioWindow {

    private final String schoolId;
    private final LectioSession session;

    public LectioWindow(@NotNull String schoolId, @NotNull LectioSession session) {
        this.schoolId = schoolId;
        this.session = session;
    }

    public @NotNull Collection<LectioCalendarEvent> getEvents(int year, int weekNum) {
        System.out.println("LINK: " + generateUrl(year, weekNum));
        session.page().navigate(generateUrl(year, weekNum));

        session.page().waitForLoadState();
        System.out.println("loaded");

        try {
            return new LectioScheduleParser().parseSchedule(session.page(), weekNum, year);
        } catch (Exception e) {
            System.err.println("Error parsing schedule: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("deprecation")
    protected String generateUrl(int year, int weekNum) {
        String weekString = String.valueOf(weekNum);
        if(weekString.length() == 1) {
            weekString = "0" + weekString;
        }
        weekString += year;
        return String.format("https://www.lectio.dk/lectio/%s/SkemaNy.aspx?showtype=0&week=%s", schoolId, weekString);
    }

    public void close() {
        session.close();
    }

}
