package me.abdiskiosk.lectiocalendar;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.SneakyThrows;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import me.abdiskiosk.lectiocalendar.lectio.storage.LectioAuthStorage;
import me.abdiskiosk.lectiocalendar.server.JavalinServer;

import java.io.File;

public class Main {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String SCHOOL_ID = dotenv.get("SCHOOL_ID");
    private static final String API_KEY = dotenv.get("API_KEY");


    @SneakyThrows
    public static void main(String[] args) {
        LectioAuthStorage auth = new LectioAuthStorage(new File("./auth.json"));
        LectioClient client = new LectioClient(SCHOOL_ID, auth);

        new JavalinServer(8080, API_KEY, client);
    }


}
