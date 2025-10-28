package me.abdiskiosk.lectiocalendar.server;

import com.microsoft.playwright.Page;
import dk.zentoc.LectioSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.abdiskiosk.lectiocalendar.lectio.LectioWindow;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ICSBrowserPage {

    private LectioWindow page1;
    private LectioWindow page2;

    public synchronized @NotNull LectioWindow getPage1() {
        return this.page1;
    }

    public synchronized @NotNull LectioWindow getPage2() {
        return this.page2;
    }

    public synchronized void setPages(@NotNull LectioWindow page1, @NotNull LectioWindow page2) {
        this.page1 = page1;
        this.page2 = page2;
    }


}
