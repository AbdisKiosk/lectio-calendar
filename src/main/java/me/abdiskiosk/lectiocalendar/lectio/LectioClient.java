package me.abdiskiosk.lectiocalendar.lectio;

import com.microsoft.playwright.Page;
import dk.zentoc.LectioLogin;
import dk.zentoc.LectioSession;
import me.abdiskiosk.lectiocalendar.calendar.LectioCalendarEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class LectioClient {

    private final String schoolId;
    private LectioSession session;

    public LectioClient(@NotNull String schoolId) {
        this.schoolId = schoolId;
        LectioLogin login = new LectioLogin();
        session = login.loginLectio(schoolId);
    }

    public @NotNull Collection<LectioCalendarEvent> getEvents(int year, int weekNum) {
        Page page = session.page();
        System.out.println("LINK: " + generateUrl(year, weekNum));
        page.navigate(generateUrl(year, weekNum));

        page.waitForLoadState();
        System.out.println("loaded");

        try {
            return new LectioScheduleParser().parseSchedule(page, weekNum, year);
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


}
