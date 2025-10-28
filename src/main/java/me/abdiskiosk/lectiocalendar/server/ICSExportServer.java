        package me.abdiskiosk.lectiocalendar.server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import me.abdiskiosk.lectiocalendar.calendar.ICSFileGenerator;
import me.abdiskiosk.lectiocalendar.calendar.LectioCalendarEvent;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import me.abdiskiosk.lectiocalendar.lectio.LectioWindow;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class ICSExportServer {

    private ICSBrowserPage pages;

    public ICSExportServer(@NotNull ICSBrowserPage pages) {
        this.pages = pages;
    }

    public synchronized void onRequest(@NotNull Context ctx) throws IOException {
        List<LectioCalendarEvent> events = new CopyOnWriteArrayList<>();
        int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        boolean isAfterNewYear = currentWeek < 32;
        int baseYear = isAfterNewYear ? year - 1 : year;


        CompletableFuture<List<LectioCalendarEvent>> c1 = CompletableFuture.supplyAsync(() -> {
            List<LectioCalendarEvent> c1Events = new ArrayList<>();
            for (int i = 32; i < 53; i++) {
                c1Events.addAll(pages.getPage1().getEvents(baseYear, i));
            }

            return c1Events;
        });

        CompletableFuture<List<LectioCalendarEvent>> c2 = CompletableFuture.supplyAsync(() -> {
            List<LectioCalendarEvent> c2Events = new ArrayList<>();
            for (int i = 1; i < 32; i++) {
                c2Events.addAll(pages.getPage2().getEvents(baseYear + 1, i));
            }

            return c2Events;
        });

        events.addAll(c1.join());
        events.addAll(c2.join());

        String ics = new ICSFileGenerator().generateIcs(events);

        ctx.contentType("text/calendar; charset=utf-8");
        ctx.header("Cache-Control", "public, max-age=3600");
        if(events.isEmpty()) {
            events.add(new LectioCalendarEvent("Fejl i kalender", null, null, null,
                    "CANCELLED", new Date(), new Date(System.currentTimeMillis() + 30 * 60 * 60 * 1000)));
        }

        ctx.result(ics);
    }

}