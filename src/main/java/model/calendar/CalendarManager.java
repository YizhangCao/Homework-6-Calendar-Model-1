package model.calendar;

import model.event.Event;
import model.event.EventBuilder;
import model.event.EventVisibility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages multiple calendars and provides save/restore functionality.
 * AI-generated implementation for calendar persistence.
 */
public class CalendarManager {

  private static final String DEFAULT_SAVE_FILE = "calendars.dat";
  private final Map<String, CalendarImpl> calendars;
  private final Path savePath;

  /**
   * Creates a new calendar manager with the default save location.
   */
  public CalendarManager() {
    this(Paths.get(System.getProperty("user.home"), ".calendar-app", DEFAULT_SAVE_FILE));
  }

  /**
   * Creates a new calendar manager with a custom save location.
   *
   * @param savePath the path to save/restore calendars
   */
  public CalendarManager(Path savePath) {
    this.calendars = new HashMap<>();
    this.savePath = savePath;
  }

  /**
   * Creates a new calendar with the given title.
   *
   * @param title the title of the calendar
   * @return the newly created calendar
   * @throws IllegalArgumentException if a calendar with the title already exists
   */
  public CalendarImpl createCalendar(String title) {
    if (calendars.containsKey(title)) {
      throw new IllegalArgumentException("Calendar with title '" + title + "' already exists");
    }
    CalendarImpl calendar = new CalendarImpl(title);
    calendars.put(title, calendar);
    return calendar;
  }

  /**
   * Creates a new calendar with the given title and conflict policy.
   *
   * @param title the title of the calendar
   * @param policy the conflict policy
   * @return the newly created calendar
   */
  public CalendarImpl createCalendar(String title, ConflictPolicy policy) {
    if (calendars.containsKey(title)) {
      throw new IllegalArgumentException("Calendar with title '" + title + "' already exists");
    }
    CalendarImpl calendar = new CalendarImpl(title, policy);
    calendars.put(title, calendar);
    return calendar;
  }

  /**
   * Gets a calendar by title.
   *
   * @param title the title of the calendar
   * @return the calendar, or null if not found
   */
  public CalendarImpl getCalendar(String title) {
    return calendars.get(title);
  }

  /**
   * Gets all calendars.
   *
   * @return a list of all calendars
   */
  public List<CalendarImpl> getAllCalendars() {
    return new ArrayList<>(calendars.values());
  }

  /**
   * Gets all calendar titles.
   *
   * @return a list of all calendar titles
   */
  public List<String> getCalendarTitles() {
    return new ArrayList<>(calendars.keySet());
  }

  /**
   * Removes a calendar by title.
   *
   * @param title the title of the calendar to remove
   * @return true if the calendar was removed, false if not found
   */
  public boolean removeCalendar(String title) {
    return calendars.remove(title) != null;
  }

  /**
   * Saves all calendars to the configured save path.
   *
   * @throws IOException if the calendars cannot be saved
   */
  public void saveAll() throws IOException {
    // Ensure parent directory exists
    Files.createDirectories(savePath.getParent());

    try (BufferedWriter writer = Files.newBufferedWriter(savePath)) {
      // Write number of calendars
      writer.write(String.valueOf(calendars.size()));
      writer.newLine();

      for (CalendarImpl calendar : calendars.values()) {
        saveCalendar(writer, calendar);
      }
    }
  }

  /**
   * Restores all calendars from the configured save path.
   *
   * @throws IOException if the calendars cannot be restored
   */
  public void restoreAll() throws IOException {
    if (!Files.exists(savePath)) {
      return; // No saved data
    }

    calendars.clear();

    try (BufferedReader reader = Files.newBufferedReader(savePath)) {
      int calendarCount = Integer.parseInt(reader.readLine().trim());

      for (int i = 0; i < calendarCount; i++) {
        CalendarImpl calendar = restoreCalendar(reader);
        calendars.put(calendar.getTitle(), calendar);
      }
    }
  }

  /**
   * Saves a single calendar to the writer.
   */
  private void saveCalendar(BufferedWriter writer, CalendarImpl calendar) throws IOException {
    // Calendar title
    writer.write("CALENDAR:" + escapeString(calendar.getTitle()));
    writer.newLine();

    // Conflict policy
    writer.write("POLICY:" + calendar.getConflictPolicy().name());
    writer.newLine();

    // Events
    List<Event> events = calendar.getAllEvents();
    writer.write("EVENTS:" + events.size());
    writer.newLine();

    for (Event event : events) {
      saveEvent(writer, event);
    }

    writer.write("END_CALENDAR");
    writer.newLine();
  }

  /**
   * Saves a single event to the writer.
   */
  private void saveEvent(BufferedWriter writer, Event event) throws IOException {
    writer.write("EVENT_START");
    writer.newLine();

    writer.write("SUBJECT:" + escapeString(event.getSubject()));
    writer.newLine();

    writer.write("START_DATE:" + event.getStartDate());
    writer.newLine();

    event.getStartTime().ifPresent(time -> {
      try {
        writer.write("START_TIME:" + time);
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    event.getEndDate().ifPresent(date -> {
      try {
        writer.write("END_DATE:" + date);
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    event.getEndTime().ifPresent(time -> {
      try {
        writer.write("END_TIME:" + time);
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    writer.write("VISIBILITY:" + event.getVisibility().name());
    writer.newLine();

    event.getDescription().ifPresent(desc -> {
      try {
        writer.write("DESCRIPTION:" + escapeString(desc));
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    event.getLocation().ifPresent(loc -> {
      try {
        writer.write("LOCATION:" + escapeString(loc));
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    writer.write("EVENT_END");
    writer.newLine();
  }

  /**
   * Restores a calendar from the reader.
   */
  private CalendarImpl restoreCalendar(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (!line.startsWith("CALENDAR:")) {
      throw new IOException("Expected CALENDAR header, got: " + line);
    }
    String title = unescapeString(line.substring("CALENDAR:".length()));

    line = reader.readLine();
    ConflictPolicy policy = ConflictPolicy.valueOf(line.substring("POLICY:".length()));

    CalendarImpl calendar = new CalendarImpl(title, policy);

    line = reader.readLine();
    int eventCount = Integer.parseInt(line.substring("EVENTS:".length()));

    for (int i = 0; i < eventCount; i++) {
      Event event = restoreEvent(reader);
      calendar.addEvent(event);
    }

    line = reader.readLine(); // END_CALENDAR
    if (!line.equals("END_CALENDAR")) {
      throw new IOException("Expected END_CALENDAR, got: " + line);
    }

    return calendar;
  }

  /**
   * Restores a single event from the reader.
   */
  private Event restoreEvent(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (!line.equals("EVENT_START")) {
      throw new IOException("Expected EVENT_START, got: " + line);
    }

    EventBuilder builder = EventBuilder.create();

    while (!(line = reader.readLine()).equals("EVENT_END")) {
      int colonIndex = line.indexOf(':');
      String key = line.substring(0, colonIndex);
      String value = line.substring(colonIndex + 1);

      switch (key) {
        case "SUBJECT":
          builder.withSubject(unescapeString(value));
          break;
        case "START_DATE":
          builder.withStartDate(LocalDate.parse(value));
          break;
        case "START_TIME":
          builder.withStartTime(LocalTime.parse(value));
          break;
        case "END_DATE":
          builder.withEndDate(LocalDate.parse(value));
          break;
        case "END_TIME":
          builder.withEndTime(LocalTime.parse(value));
          break;
        case "VISIBILITY":
          builder.withVisibility(EventVisibility.valueOf(value));
          break;
        case "DESCRIPTION":
          builder.withDescription(unescapeString(value));
          break;
        case "LOCATION":
          builder.withLocation(unescapeString(value));
          break;
        default:
          // Unknown field, skip
          break;
      }
    }

    return builder.build();
  }

  /**
   * Escapes special characters in strings for saving.
   */
  private String escapeString(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
  }

  /**
   * Unescapes special characters in strings when restoring.
   */
  private String unescapeString(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\");
  }
}
