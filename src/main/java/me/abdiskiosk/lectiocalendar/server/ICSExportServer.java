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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

        public class ICSExportServer {

    private final LectioClient lectioClient;
    private final Javalin app;

    LectioWindow window1;
    LectioWindow window2;

    public ICSExportServer(int port, @NotNull String auth, @NotNull LectioClient lectioClient) {
        this.lectioClient = lectioClient;
        app = Javalin.create(config ->
                config.router.mount(router ->
                        router.beforeMatched(ctx -> {
                            String provided = ctx.queryParam("auth");
                            if (provided == null || !provided.equals(auth)) {
                                throw new UnauthorizedResponse();
                            }
                        })));
        app.start(port);

        app.get("/schedule", this::onRequest);

        window1 = lectioClient.openWindow();
        window2 = lectioClient.openWindow();
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
                c1Events.addAll(window1.getEvents(baseYear, i));
            }

            return c1Events;
        });

        CompletableFuture<List<LectioCalendarEvent>> c2 = CompletableFuture.supplyAsync(() -> {
            List<LectioCalendarEvent> c2Events = new ArrayList<>();
            for (int i = 1; i < 32; i++) {
                c2Events.addAll(window2.getEvents(baseYear + 1, i));
            }

            return c2Events;
        });

        events.addAll(c1.join());
        events.addAll(c2.join());

        String ics = new ICSFileGenerator().generateIcs(events);

        ctx.contentType("text/calendar; charset=utf-8");
        ctx.header("Cache-Control", "public, max-age=3600");
        ctx.result(ics);
    }

}