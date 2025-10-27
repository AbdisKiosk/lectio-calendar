package me.abdiskiosk.lectiocalendar.lectio;

import com.microsoft.playwright.Page;
import dk.zentoc.LectioSession;
import lombok.SneakyThrows;
import me.abdiskiosk.lectiocalendar.lectio.storage.LectioAuthStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.zip.DataFormatException;

public class LectioClient {

    private final String schoolId;
    private final LectioAuthStorage authStorage;

    public LectioClient(@NotNull String schoolId, @NotNull LectioAuthStorage authStorage) throws IOException,
            DataFormatException {
        this.schoolId = schoolId;
        this.authStorage = authStorage;
    }

    @SneakyThrows
    public @NotNull LectioWindow openWindow() {
        LectioSession session = authStorage.loadOrCreate(schoolId);
        authStorage.save(session);
        return new LectioWindow(schoolId, session);
    }


}
