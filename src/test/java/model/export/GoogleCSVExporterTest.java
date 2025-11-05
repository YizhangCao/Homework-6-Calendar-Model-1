package model.export;

import model.calendar.CalendarImpl;
import model.event.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class GoogleCSVExporterTest {
  private CalendarImpl calendar;
  private GoogleCSVExporter exporter;

  @BeforeEach
  void setUp() {
    calendar = new CalendarImpl("Export Test");
    exporter = new GoogleCSVExporter();
  }

  @Test
  void testExportEmptyCalendar() {
    String csv = exporter.export(calendar);

    String[] lines = csv.split("\n");
    assertEquals(1, lines.length);
    assertTrue(lines[0].startsWith("Subject,Start Date"));
  }

  @Test
  void testExportSingleEvent() {
    Event event = EventBuilder.create()
        .withSubject("Test Meeting")
        .withStartDate(LocalDate.of(2025, 3, 15))
        .withStartTime(LocalTime.of(14, 30))
        .withEndTime(LocalTime.of(15, 30))
        .withDescription("Important meeting")
        .withLocation("Conference Room A")
        .withVisibility(EventVisibility.PRIVATE)
        .build();

    calendar.addEvent(event);
    String csv = exporter.export(calendar);

    String[] lines = csv.split("\n");
    assertEquals(2, lines.length);

    String eventLine = lines[1];
    assertTrue(eventLine.contains("Test Meeting"));
    assertTrue(eventLine.contains("03/15/2025"));
    assertTrue(eventLine.contains("02:30 PM"));
    assertTrue(eventLine.contains("03:30 PM"));
    assertTrue(eventLine.contains("Important meeting"));
    assertTrue(eventLine.contains("Conference Room A"));
    assertTrue(eventLine.contains("TRUE")); // Private
  }

  @Test
  void testExportAllDayEvent() {
    Event event = EventBuilder.create()
        .withSubject("Holiday")
        .withStartDate(LocalDate.of(2025, 12, 25))
        .withDescription("Christmas")
        .build();

    calendar.addEvent(event);
    String csv = exporter.export(calendar);

    String[] lines = csv.split("\n");
    String eventLine = lines[1];

    assertTrue(eventLine.contains("Holiday"));
    assertTrue(eventLine.contains("12/25/2025"));
    assertTrue(eventLine.contains("TRUE")); // All day event
  }

  @Test
  void testExportWithSpecialCharacters() {
    Event event = EventBuilder.create()
        .withSubject("Meeting, with comma")
        .withStartDate(LocalDate.of(2025, 1, 1))
        .withDescription("Description with \"quotes\" and\nnewline")
        .build();

    calendar.addEvent(event);
    String csv = exporter.export(calendar);

    // Verify CSV escaping
    assertTrue(csv.contains("\"Meeting, with comma\""));
    assertTrue(csv.contains("\"Description with \"\"quotes\"\" and\nnewline\""));
  }

  @Test
  void testExportMultipleEvents() {
    Event event1 = EventBuilder.create()
        .withSubject("Morning Meeting")
        .withStartDate(LocalDate.of(2025, 1, 15))
        .withStartTime(LocalTime.of(9, 0))
        .withEndTime(LocalTime.of(10, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Lunch")
        .withStartDate(LocalDate.of(2025, 1, 15))
        .withStartTime(LocalTime.of(12, 0))
        .withEndTime(LocalTime.of(13, 0))
        .build();

    Event event3 = EventBuilder.create()
        .withSubject("All Day Event")
        .withStartDate(LocalDate.of(2025, 1, 16))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    String csv = exporter.export(calendar);
    String[] lines = csv.split("\n");

    assertEquals(4, lines.length); // Header + 3 events
  }
}