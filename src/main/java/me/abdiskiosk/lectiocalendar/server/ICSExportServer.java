        package me.abdiskiosk.lectiocalendar.server;

import io.javalin.http.Context;
import me.abdiskiosk.lectiocalendar.Main;
import me.abdiskiosk.lectiocalendar.calendar.ICSFileGenerator;
import me.abdiskiosk.lectiocalendar.db.dao.LectioCalendarEventDAO;
import me.abdiskiosk.lectiocalendar.db.object.LectioCalendarEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

        public class ICSExportServer {

    private final LectioCalendarEventDAO calendarEvents;

    public ICSExportServer(@NotNull LectioCalendarEventDAO pages) {
        this.calendarEvents = pages;
    }

    public synchronized void onRequest(@NotNull Context ctx) throws SQLException, IOException {
        List<LectioCalendarEvent> events =  new ArrayList<>();
        LectioCalendarEvent notLoggedInEvent = new LectioCalendarEvent(-1, null, null, null, null, null,
                -1, new Date(), new Date(System.currentTimeMillis() + 60 * 60 * 1000), new Date());
        if(Main.isHasError()) {
            events.add(notLoggedInEvent);
        }
        events.addAll(calendarEvents.queryEvents());

        String ics = new ICSFileGenerator().generateIcs(events);

        ctx.contentType("text/calendar; charset=utf-8");
        ctx.header("Cache-Control", "public, max-age=3600");

        ctx.result(ics);
    }

}