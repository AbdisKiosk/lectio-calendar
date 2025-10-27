package me.abdiskiosk.lectiocalendar;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.SneakyThrows;
import me.abdiskiosk.lectiocalendar.calendar.ICSFileGenerator;
import me.abdiskiosk.lectiocalendar.calendar.LectioCalendarEvent;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import me.abdiskiosk.lectiocalendar.server.ICSExportServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String SCHOOL_ID = dotenv.get("SCHOOL_ID");
    private static final String API_KEY = dotenv.get("API_KEY");


    @SneakyThrows
    public static void main(String[] args) {
        LectioClient client = new LectioClient(SCHOOL_ID);
        new ICSExportServer(8080, API_KEY, client);
    }


}
