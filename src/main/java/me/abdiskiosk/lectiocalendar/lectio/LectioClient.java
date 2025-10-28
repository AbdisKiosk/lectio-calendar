package me.abdiskiosk.lectiocalendar.lectio;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import dk.zentoc.LectioSession;
import dk.zentoc.LectioSessionData;
import lombok.SneakyThrows;
import me.abdiskiosk.lectiocalendar.lectio.storage.LectioAuthStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

public class LectioClient {

    private final String schoolId;
    private final LectioAuthStorage authStorage;

    public LectioClient(@NotNull String schoolId, @NotNull LectioAuthStorage authStorage) throws IOException,
            DataFormatException {
        this.schoolId = schoolId;
        this.authStorage = authStorage;
    }

    public void setCookies(@NotNull List<Cookie> cookies) throws IOException {
        LectioSessionData data = new LectioSessionData();
        data.setCookies(cookies);
        data.setSchoolId(schoolId);
        authStorage.save(data);
    }

    @SneakyThrows
    public @NotNull LectioWindow openWindow() {
        LectioSession session = authStorage.loadOrCreate(schoolId);
        authStorage.save(session);
        return new LectioWindow(schoolId, session);
    }


}
