package me.abdiskiosk.lectiocalendar.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.javalin.http.Context;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LectioUpdateCookieServer {

    private final LectioClient client;

    public LectioUpdateCookieServer(@NotNull LectioClient client) {
        this.client = client;
    }

    public synchronized void onRequest(@NotNull Context ctx) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.readValue(ctx.body(), ObjectNode.class);

        System.out.println(json.toString());
    }

}
