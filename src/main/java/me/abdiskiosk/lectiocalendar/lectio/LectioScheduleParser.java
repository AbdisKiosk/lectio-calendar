package me.abdiskiosk.lectiocalendar.lectio;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import me.abdiskiosk.lectiocalendar.calendar.LectioCalendarEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


//lavet af deepseek
public class LectioScheduleParser {

    public Collection<LectioCalendarEvent> parseSchedule(Page page, int weekNum, int year) {
        List<LectioCalendarEvent> events = new ArrayList<>();

        try {
            page.waitForSelector(".s2skemabrik.s2normal.s2brik",
                    new Page.WaitForSelectorOptions().setTimeout(200));
        } catch (PlaywrightException e) {
            System.err.println("Timeout waiting for schedule table to load. Returning empty collection.");
            return events; // empty list
        }

        // Get all schedule bricks using Playwright
        var brickElements = new ArrayList<>(page.querySelectorAll(".s2skemabrik.s2normal.s2brik"));

        brickElements.addAll(page.querySelectorAll(".s2skemabrik.s2changed.s2brik"));

        for (var brick : brickElements) {
            try {
                LectioCalendarEvent event = parseScheduleBrick(brick, weekNum, year, null);
                if (event != null) {
                    events.add(event);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse schedule brick: " + e.getMessage());
            }
        }

        var cancelledBrickElements = new ArrayList<>(page.querySelectorAll(".s2skemabrik.s2cancelled.s2brik"));
        for (var brick : cancelledBrickElements) {
            try {
                LectioCalendarEvent event = parseScheduleBrick(brick, weekNum, year, "CANCELLED");
                if (event != null) {
                    events.add(event);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse schedule brick: " + e.getMessage());
            }
        }

        return events;
    }


    private LectioCalendarEvent parseScheduleBrick(ElementHandle brick, int weekNum, int year, @Nullable String status)
            throws ParseException {
        System.out.println(brick.innerHTML());
        System.out.println(weekNum);
        // Extract tooltip data
        String tooltip = brick.getAttribute("data-tooltip");
        if (tooltip == null || tooltip.isEmpty()) {
            return null;
        }

        // Parse the tooltip to extract date, time, and other details
        TooltipData tooltipData = parseTooltip(tooltip);
        if (tooltipData == null) {
            return null;
        }

        // Extract content from the brick using Playwright
        //BrickContent brickContent = parseBrickContent(brick);

        // Parse dates
        Date[] dates = parseDates(tooltipData.dateTimeInfo, weekNum, year);
        if (dates == null) {
            return null;
        }

        return new LectioCalendarEvent(
                tooltipData.title,
                tooltipData.team,
                tooltipData.teachers,
                tooltipData.room,
                status,
                dates[0],
                dates[1]
        );
    }

    private TooltipData parseTooltip(String tooltip) {
        if(tooltip.contains("Aflyst!")) {
            tooltip = tooltip.replace("Aflyst   !\n", "");
        }
        if(tooltip.contains("Ændret!")) {
            tooltip = tooltip.replace("Ændret!\n", "");
        }
        String[] lines = tooltip.split("\n");
        TooltipData data = new TooltipData();
        // The second line typically contains the team/class
        // Find teacher and room information
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("/") && line.contains("til") && line.contains(":")) {
                data.dateTimeInfo = line.trim(); //todo: regex
            }
            if (line.startsWith("Lærer:") || line.startsWith("Lærere:")) {
                data.teachers = line.replace("Lærer:", "").replace("Lærere:", "").trim();
            } else if (line.startsWith("Lokale:")) {
                data.room = line.replace("Lokale:", "").trim();
            } else if (line.startsWith("Hold:")) {
                data.team = line.replaceAll("Hold:", "");
            } else if (!line.isEmpty() && !line.startsWith("Lektier:") && !line.startsWith("Note:") && !line.startsWith("Øvrigt indhold:") && !line.contains(":")) {
                // This is likely the title if we haven't found one yet
                if (data.title == null) {
                    data.title = line;
                }
            }
        }

            return data;
    }

    private BrickContent parseBrickContent(ElementHandle brick) {
        BrickContent content = new BrickContent();

        // Extract the main content text
        String textContent = brick.textContent().trim();
        String[] parts = textContent.split("•");

        if (parts.length >= 1) {
            content.team = parts[0].trim();
        }
        if (parts.length >= 2) {
            content.teachers = parts[1].trim();
        }
        if (parts.length >= 3) {
            content.room = parts[2].trim();
        }

        // Look for title in span elements
        var titleElement = brick.querySelector("span[style*='word-wrap']");
        if (titleElement != null) {
            content.title = titleElement.textContent().trim();
        }

        return content;
    }

    private Date[] parseDates(String dateTimeInfo, int weekNum, int year) throws ParseException {
        // Handle different date formats from the tooltip
        // Format examples:
        // - "20/10-2025 08:15 til 09:15"
        // - "20/10-2025 Hele dagen"
        // - "fr 7/11 18:00 - 20:00"

        if (dateTimeInfo.contains("Hele dagen")) {
            // For all-day events
            String datePart = dateTimeInfo.split(" ")[0];
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM-yyyy");
            Date date = dateFormat.parse(datePart);

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(date);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(date);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);

            return new Date[]{startCal.getTime(), endCal.getTime()};
        } else if (dateTimeInfo.matches(".*[a-z]{2} \\d{1,2}/\\d{1,2}.*")) {
            // Handle format like "fr 7/11 18:00 - 20:00"
            return parseDayMonthTimeFormat(dateTimeInfo, year);
        } else if (dateTimeInfo.contains("/") && dateTimeInfo.contains("-") && dateTimeInfo.contains("til")) {
            // Handle format like "20/10-2025 08:15 til 09:15"
            String[] parts = dateTimeInfo.split(" til ");
            if (parts.length != 2) return null;

            String startDateTimeStr = parts[0].trim(); // e.g. "20/10-2025 08:15"
            String endPart = parts[1].trim();          // e.g. "09:15" or "20/10-2025 09:15"

            SimpleDateFormat fullFormat = new SimpleDateFormat("dd/MM-yyyy HH:mm");
            fullFormat.setLenient(false);

            Date startDate = fullFormat.parse(startDateTimeStr);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            Calendar endCal = (Calendar) startCal.clone();
            // If endPart is just a time, apply it to the start date; otherwise parse full datetime
            if (endPart.matches("\\d{1,2}:\\d{2}")) {
                String[] timeParts = endPart.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                endCal.set(Calendar.HOUR_OF_DAY, hour);
                endCal.set(Calendar.MINUTE, minute);
                endCal.set(Calendar.SECOND, 0);
                endCal.set(Calendar.MILLISECOND, 0);
            } else {
                Date endDate = fullFormat.parse(endPart);
                endCal.setTime(endDate);
                endCal.set(Calendar.SECOND, 0);
                endCal.set(Calendar.MILLISECOND, 0);
            }

            return new Date[]{startCal.getTime(), endCal.getTime()};
        } else {
            // Try other formats or return null if cannot parse
            return null;
        }
    }

    public Date dateFromWeekAndTime(int weekNum, int year, String timeString) {
        if (timeString == null) throw new IllegalArgumentException("timeString is null");
        String[] parts = timeString.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("timeString must be in HH:mm format");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.clear();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4); // ISO week definition
        cal.setWeekDate(year, weekNum, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date[] parseDayMonthTimeFormat(String dateTimeInfo, int year) throws ParseException {
        // Parse format like "fr 7/11 18:00 - 20:00"
        String[] parts = dateTimeInfo.split(" ");

        if (parts.length < 5) return null;

        // Extract day of week (ignored for now, we'll use the date)
        String dayOfWeek = parts[0]; // e.g., "fr"

        // Extract date part "7/11"
        String datePart = parts[1];
        String[] dateParts = datePart.split("/");
        if (dateParts.length != 2) return null;

        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);

        // Extract start time "18:00"
        String startTimeStr = parts[2];

        // Extract end time "20:00" (it's usually the last part)
        String endTimeStr = parts[parts.length - 1];

        // Parse times
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // Create calendar instances for start and end
        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month - 1, day); // month is 0-based in Calendar

        // Parse and set start time
        Date startTime = timeFormat.parse(startTimeStr);
        Calendar startTimeCal = Calendar.getInstance();
        startTimeCal.setTime(startTime);
        startCal.set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY));
        startCal.set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE));
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month - 1, day);

        // Parse and set end time
        Date endTime = timeFormat.parse(endTimeStr);
        Calendar endTimeCal = Calendar.getInstance();
        endTimeCal.setTime(endTime);
        endCal.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
        endCal.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));
        endCal.set(Calendar.SECOND, 0);

        return new Date[]{startCal.getTime(), endCal.getTime()};
    }

    // Helper classes to store parsed data
    private static class TooltipData {
        String dateTimeInfo;
        String title;
        String team;
        String teachers;
        String room;
    }

    private static class BrickContent {
        String title;
        String team;
        String teachers;
        String room;
    }
}