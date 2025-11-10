package model.export;

import model.calendar.Calendar;
import model.calendar.CalendarImpl;
import model.event.Event;
import model.event.EventVisibility;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

/**
 * Exports calendars to Google Calendar CSV format.
 */
public class GoogleCSVExporter implements CalendarExporter {
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("hh:mm a");
  private static final String CSV_HEADER =
      "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private";

  @Override
  public String export(Calendar calendar) {
    StringBuilder csv = new StringBuilder();
    csv.append(CSV_HEADER).append("\n");

    List<Event> events = calendar instanceof CalendarImpl
        ? ((CalendarImpl) calendar).getAllEvents()
        : List.of();

    for (Event event : events) {
      csv.append(exportEvent(event)).append("\n");
    }

    return csv.toString();
  }

  /**
   * Exports a single event to CSV format.
   */
  private String exportEvent(Event event) {
    StringJoiner joiner = new StringJoiner(",");

    // Subject
    joiner.add(escapeCSV(event.getSubject()));

    // Start Date
    joiner.add(event.getStartDate().format(DATE_FORMAT));

    // Start Time
    joiner.add(event.getStartTime()
        .map(time -> time.format(TIME_FORMAT))
        .orElse(""));

    // End Date
    joiner.add(event.getEndDate()
        .map(date -> date.format(DATE_FORMAT))
        .orElse(event.getStartDate().format(DATE_FORMAT)));

    // End Time
    joiner.add(event.getEndTime()
        .map(time -> time.format(TIME_FORMAT))
        .orElse(""));

    // All Day Event
    joiner.add(event.isAllDay() ? "TRUE" : "FALSE");

    // Description
    joiner.add(escapeCSV(event.getDescription().orElse("")));

    // Location
    joiner.add(escapeCSV(event.getLocation().orElse("")));

    // Private
    joiner.add(event.getVisibility() == EventVisibility.PRIVATE ? "TRUE" : "FALSE");

    return joiner.toString();
  }

  /**
   * Escapes CSV special characters.
   */
  private String escapeCSV(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}