package model.export;

import model.calendar.Calendar;
import model.calendar.CalendarImpl;
import model.event.EventBuilder;
import model.event.EventVisibility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports calendars from Google Calendar CSV format.
 * AI-generated implementation for CSV parsing.
 */
public class GoogleCSVImporter {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Imports events from CSV content into a new calendar.
   *
   * @param title the title for the new calendar
   * @param csvContent the CSV content to import
   * @return a new calendar containing the imported events
   * @throws IOException if the CSV content cannot be parsed
   */
  public Calendar importCalendar(String title, String csvContent) throws IOException {
    CalendarImpl calendar = new CalendarImpl(title);
    List<String[]> rows = parseCSV(csvContent);

    // Skip header row
    for (int i = 1; i < rows.size(); i++) {
      String[] row = rows.get(i);
      if (row.length >= 9) {
        try {
          EventBuilder builder = parseEvent(row);
          calendar.addEvent(builder.build());
        } catch (Exception e) {
          // Skip invalid rows but continue processing
          System.err.println("Skipping invalid row " + i + ": " + e.getMessage());
        }
      }
    }

    return calendar;
  }

  /**
   * Imports events from CSV content into an existing calendar.
   *
   * @param calendar the calendar to add events to
   * @param csvContent the CSV content to import
   * @return the number of events successfully imported
   * @throws IOException if the CSV content cannot be parsed
   */
  public int importIntoCalendar(CalendarImpl calendar, String csvContent) throws IOException {
    List<String[]> rows = parseCSV(csvContent);
    int imported = 0;

    // Skip header row
    for (int i = 1; i < rows.size(); i++) {
      String[] row = rows.get(i);
      if (row.length >= 9) {
        try {
          EventBuilder builder = parseEvent(row);
          if (calendar.addEvent(builder.build())) {
            imported++;
          }
        } catch (Exception e) {
          // Skip invalid rows
          System.err.println("Skipping invalid row " + i + ": " + e.getMessage());
        }
      }
    }

    return imported;
  }

  /**
   * Parses a CSV row into an EventBuilder.
   */
  private EventBuilder parseEvent(String[] row) {
    EventBuilder builder = EventBuilder.create();

    // Subject (required)
    builder.withSubject(unescapeCSV(row[0]));

    // Start Date (required)
    builder.withStartDate(parseDate(row[1]));

    // Start Time (optional)
    if (!row[2].isEmpty()) {
      builder.withStartTime(parseTime(row[2]));
    }

    // End Date (optional)
    if (!row[3].isEmpty()) {
      builder.withEndDate(parseDate(row[3]));
    }

    // End Time (optional)
    if (!row[4].isEmpty()) {
      builder.withEndTime(parseTime(row[4]));
    }

    // All Day Event - handled by presence/absence of start time

    // Description (optional)
    if (row.length > 6 && !row[6].isEmpty()) {
      builder.withDescription(unescapeCSV(row[6]));
    }

    // Location (optional)
    if (row.length > 7 && !row[7].isEmpty()) {
      builder.withLocation(unescapeCSV(row[7]));
    }

    // Private (optional)
    if (row.length > 8 && "TRUE".equalsIgnoreCase(row[8])) {
      builder.withVisibility(EventVisibility.PRIVATE);
    }

    return builder;
  }

  /**
   * Parses a date string.
   */
  private LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
    } catch (DateTimeParseException e) {
      // Try ISO format as fallback
      return LocalDate.parse(dateStr.trim());
    }
  }

  /**
   * Parses a time string.
   */
  private LocalTime parseTime(String timeStr) {
    try {
      return LocalTime.parse(timeStr.trim(), TIME_FORMAT);
    } catch (DateTimeParseException e) {
      // Try ISO format as fallback
      return LocalTime.parse(timeStr.trim());
    }
  }

  /**
   * Parses CSV content into rows.
   * Handles quoted fields and escaped quotes.
   */
  private List<String[]> parseCSV(String content) throws IOException {
    List<String[]> rows = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new StringReader(content));
    String line;

    while ((line = reader.readLine()) != null) {
      if (!line.trim().isEmpty()) {
        rows.add(parseCSVLine(line));
      }
    }

    return rows;
  }

  /**
   * Parses a single CSV line into fields.
   * Handles quoted fields and escaped quotes.
   */
  private String[] parseCSVLine(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (inQuotes) {
        if (c == '"') {
          // Check for escaped quote
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            current.append('"');
            i++; // Skip next quote
          } else {
            inQuotes = false;
          }
        } else {
          current.append(c);
        }
      } else {
        if (c == '"') {
          inQuotes = true;
        } else if (c == ',') {
          fields.add(current.toString());
          current = new StringBuilder();
        } else {
          current.append(c);
        }
      }
    }

    fields.add(current.toString());
    return fields.toArray(new String[0]);
  }

  /**
   * Unescapes a CSV field value.
   */
  private String unescapeCSV(String value) {
    if (value == null) {
      return "";
    }
    // Remove surrounding quotes if present
    if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
    }
    // Unescape doubled quotes
    return value.replace("\"\"", "\"");
  }
}
