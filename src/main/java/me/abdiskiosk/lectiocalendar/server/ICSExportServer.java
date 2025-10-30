        package me.abdiskiosk.lectiocalendar.server;

import io.javalin.http.Context;
import me.abdiskiosk.lectiocalendar.calendar.ICSFileGenerator;
import me.abdiskiosk.lectiocalendar.db.dao.LectioCalendarEventDAO;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

public class ICSExportServer {

    private final LectioCalendarEventDAO calendarEvents;

    public ICSExportServer(@NotNull LectioCalendarEventDAO pages) {
        this.calendarEvents = pages;
    }

    public synchronized void onRequest(@NotNull Context ctx) throws SQLException, IOException {
        String ics = new ICSFileGenerator().generateIcs(calendarEvents.queryEvents());

        ctx.contentType("text/calendar; charset=utf-8");
        ctx.header("Cache-Control", "public, max-age=3600");

        ctx.result(ics);
    }

}