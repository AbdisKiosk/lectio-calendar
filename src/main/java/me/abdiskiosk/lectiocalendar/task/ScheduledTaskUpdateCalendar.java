package me.abdiskiosk.lectiocalendar.task;

import lombok.SneakyThrows;
import me.abdiskiosk.lectiocalendar.Main;
import me.abdiskiosk.lectiocalendar.db.dao.LectioCalendarEventDAO;
import me.abdiskiosk.lectiocalendar.db.object.LectioCalendarEvent;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import me.abdiskiosk.lectiocalendar.lectio.LectioWindow;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;

public class ScheduledTaskUpdateCalendar implements Runnable {

    private static final long ALL_EVENTS_UPDATE_DELAY = 60 * 60 * 24 * 3 * 1000L;
    private static final long CURRENT_WEEK_UPDATE_DELAY = 60 * 25 * 1000L;

    private final LectioCalendarEventDAO calendarEventDAO;
    private final LectioClient lectioClient;

    public ScheduledTaskUpdateCalendar(@NotNull LectioCalendarEventDAO calendarEventDAO,
                                       @NotNull LectioClient lectioClient) {
        this.calendarEventDAO = calendarEventDAO;
        this.lectioClient = lectioClient;
    }

    @SneakyThrows
    public void run() {
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        Date allEventsLastUpdated = calendarEventDAO.getOldest();
        Date currentWeekLastUpdated = calendarEventDAO.getOldest(currentWeek);

        boolean updateAllEvents = allEventsLastUpdated == null ||
                allEventsLastUpdated.before(new Date(System.currentTimeMillis() - ALL_EVENTS_UPDATE_DELAY));
        boolean updateCurrentWeek = currentWeekLastUpdated == null ||
                currentWeekLastUpdated.before(new Date(System.currentTimeMillis() - CURRENT_WEEK_UPDATE_DELAY));

        try {
            if (updateAllEvents) {
                updateAllEvents();
            }

            if (updateCurrentWeek) {
                updateCurrentWeek();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Main.setHasError(true);
        }

    }

    public void updateAllEvents() throws Exception {
        calendarEventDAO.removeAll();
        LectioWindow window = lectioClient.openWindow();
        List<LectioCalendarEvent> events = new CopyOnWriteArrayList<>();
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        boolean isAfterNewYear = currentWeek < 32;
        int baseYear = isAfterNewYear ? year - 1 : year;


        for (int i = 32; i < 53; i++) {
            events.addAll(window.getEvents(baseYear, i));
        }

        for (int i = 1; i < 32; i++) {
            events.addAll(window.getEvents(baseYear + 1, i));
        }
        window.close();


        for(LectioCalendarEvent event : events) {
            //TODO: fix
            calendarEventDAO.insert(event.getTitle(), event.getTeam(), event.getTeachers(), event.getRoom(),
                    event.getState(), event.getQueriedWeekNum(), event.getStart(), event.getEnd());
        }
    }

    public void updateCurrentWeek() throws Exception {
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        boolean isAfterNewYear = currentWeek < 32;
        int baseYear = isAfterNewYear ? year - 1 : year;


        LectioWindow window = lectioClient.openWindow();
        Collection<LectioCalendarEvent> events = window.getEvents(baseYear , currentWeek);
        window.close();

        calendarEventDAO.removeWeek(currentWeek);
        for (LectioCalendarEvent event : events) {
            calendarEventDAO.insert(event.getTitle(), event.getTeam(), event.getTeachers(), event.getRoom(),
                    event.getState(), event.getQueriedWeekNum(), event.getStart(), event.getEnd());
        }
    }

}