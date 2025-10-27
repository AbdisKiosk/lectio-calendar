package me.abdiskiosk.lectiocalendar.lectio.storage;

import dk.zentoc.LectioLogin;
import dk.zentoc.LectioSession;
import dk.zentoc.LectioSessionDataService;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.DataFormatException;

public class LectioAuthStorage {

    private final File saveFile;

    public LectioAuthStorage(@NotNull File saveFile) {
        this.saveFile = saveFile;
    }

    public void save(@NotNull LectioSession session) throws IOException {
        new LectioSessionDataService().saveFromSession(session, saveFile.toPath());
    }

    public @NotNull LectioSession loadOrCreate(@NotNull String schoolId) throws IOException, DataFormatException {
        LectioLogin lectioLogin = new LectioLogin();

        LectioSession session;
        try {
            session = lectioLogin.lectioSessionFromFile(saveFile.toPath());
        } catch (IOException | DataFormatException __) {
            session = null;
        }
        if(session == null) {
            session = lectioLogin.loginLectio(schoolId);
        }

        return session;
    }

}
