package me.abdiskiosk.lectiocalendar.server;

import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import org.jetbrains.annotations.NotNull;

public class JavalinServer {

    private final LectioClient lectioClient;
    private final Javalin app;

    public JavalinServer(int port, @NotNull String auth, @NotNull LectioClient lectioClient) {
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


        ICSBrowserPage pages = new ICSBrowserPage(lectioClient.openWindow(), lectioClient.openWindow());
        ICSExportServer exportServer = new ICSExportServer(pages);

        app.get("/schedule", exportServer::onRequest);

    }

}
