package integration;

import model.calendar.*;
import model.event.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CalendarIntegrationTest {
  private CalendarImpl calendar;

  @BeforeEach
  void setUp() {
    calendar = new CalendarImpl("Integration Test Calendar");
  }

  @Test
  void testCompleteWorkflow() {
    // Create single events
    Event meeting = EventBuilder.create()
        .withSubject("Team Meeting")
        .withStartDate(LocalDate.of(2025, 2, 1))
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .withLocation("Room 101")
        .withDescription("Weekly sync")
        .build();

    Event lunch = EventBuilder.create()
        .withSubject("Team Lunch")
        .withStartDate(LocalDate.of(2025, 2, 1))
        .withStartTime(LocalTime.of(12, 0))
        .withEndTime(LocalTime.of(13, 0))
        .withLocation("Cafeteria")
        .build();

    assertTrue(calendar.addEvent(meeting));
    assertTrue(calendar.addEvent(lunch));

    // Add recurring event
    EventBuilder recurring = EventBuilder.create()
        .withSubject("Daily Standup")
        .withStartDate(LocalDate.of(2025, 2, 3))
        .withStartTime(LocalTime.of(9, 0))
        .withEndTime(LocalTime.of(9, 15));

    RecurrencePattern pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY),
        10
    );

    RecurringEvent standup = calendar.addRecurringEvent(
        recurring, pattern, LocalDate.of(2025, 2, 3)
    );

    assertNotNull(standup);

    // Query events
    List<Event> febEvents = calendar.getEventsInRange(
        LocalDate.of(2025, 2, 1),
        LocalDate.of(2025, 2, 28)
    );

    assertTrue(febEvents.size() > 2);

    // Check busy status
    assertTrue(calendar.isBusy(
        LocalDate.of(2025, 2, 1),
        LocalTime.of(10, 30)
    ));

    assertFalse(calendar.isBusy(
        LocalDate.of(2025, 2, 1),
        LocalTime.of(14, 0)
    ));

    // Update an event
    EventBuilder updateBuilder = EventBuilder.from(meeting)
        .withLocation("Room 202")
        .withEndTime(LocalTime.of(11, 30));

    assertTrue(calendar.updateEvent(meeting, updateBuilder));

    // Export to CSV
    String csv = calendar.exportToCSV();
    assertNotNull(csv);
    assertTrue(csv.contains("Team Meeting"));
    assertTrue(csv.contains("Daily Standup"));
  }

  @Test
  void testRecurringEventModifications() {
    // Create a recurring event
    EventBuilder base = EventBuilder.create()
        .withSubject("Weekly Review")
        .withStartDate(LocalDate.of(2025, 1, 6))
        .withStartTime(LocalTime.of(15, 0))
        .withEndTime(LocalTime.of(16, 0))
        .withLocation("Office");

    RecurrencePattern pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.MONDAY),
        4
    );

    RecurringEvent weekly = calendar.addRecurringEvent(
        base,
        pattern,
        LocalDate.of(2025, 1, 6)
    );

    // Modify single instance
    LocalDate firstMonday = LocalDate.of(2025, 1, 6);
    EventBuilder singleUpdate = EventBuilder.create()
        .withSubject("Special Review")
        .withStartDate(firstMonday)
        .withStartTime(LocalTime.of(14, 0))
        .withEndTime(LocalTime.of(15, 0));

    weekly.modifyInstance(firstMonday, singleUpdate);

    // Verify modification
    Event modified = weekly.getInstance(firstMonday).orElse(null);
    assertNotNull(modified);
    assertEquals("Special Review", modified.getSubject());

    // Verify other instances unchanged
    LocalDate secondMonday = LocalDate.of(2025, 1, 13);
    Event unchanged = weekly.getInstance(secondMonday).orElse(null);
    assertNotNull(unchanged);
    assertEquals("Weekly Review", unchanged.getSubject());

    // Modify all from a date
    EventBuilder fromUpdate = EventBuilder.create()
        .withSubject("Updated Review")
        .withStartDate(secondMonday)
        .withStartTime(LocalTime.of(16, 0))
        .withEndTime(LocalTime.of(17, 0));

    weekly.modifyFrom(secondMonday, fromUpdate);

    // Verify changes
    Event afterUpdate = weekly.getInstance(LocalDate.of(2025, 1, 20)).orElse(null);
    assertNotNull(afterUpdate);
    assertEquals("Updated Review", afterUpdate.getSubject());
  }

  @Test
  void testConflictScenarios() {
    CalendarImpl strictCalendar = new CalendarImpl(
        "Strict Calendar",
        ConflictPolicy.REJECT_CONFLICTS
    );

    CalendarImpl lenientCalendar = new CalendarImpl(
        "Lenient Calendar",
        ConflictPolicy.ALLOW_CONFLICTS
    );

    Event event1 = EventBuilder.create()
        .withSubject("Event 1")
        .withStartDate(LocalDate.of(2025, 3, 1))
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(12, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Event 2")
        .withStartDate(LocalDate.of(2025, 3, 1))
        .withStartTime(LocalTime.of(11, 0))
        .withEndTime(LocalTime.of(13, 0))
        .build();

    // Strict calendar rejects conflicts
    assertTrue(strictCalendar.addEvent(event1));
    assertFalse(strictCalendar.addEvent(event2));
    assertEquals(1, strictCalendar.getAllEvents().size());

    // Lenient calendar allows conflicts
    assertTrue(lenientCalendar.addEvent(event1));
    assertTrue(lenientCalendar.addEvent(event2));
    assertEquals(2, lenientCalendar.getAllEvents().size());
  }

  @Test
  void testEdgeCases() {
    // Multi-day event
    Event multiDay = EventBuilder.create()
        .withSubject("Conference")
        .withStartDate(LocalDate.of(2025, 5, 1))
        .withStartTime(LocalTime.of(9, 0))
        .withEndDate(LocalDate.of(2025, 5, 3))
        .withEndTime(LocalTime.of(17, 0))
        .build();

    assertTrue(calendar.addEvent(multiDay));

    // Event should appear in queries for all covered days
    assertTrue(calendar.getEventsOnDate(LocalDate.of(2025, 5, 1)).contains(multiDay));
    assertTrue(calendar.getEventsOnDate(LocalDate.of(2025, 5, 2)).contains(multiDay));
    assertTrue(calendar.getEventsOnDate(LocalDate.of(2025, 5, 3)).contains(multiDay));

    // Event at midnight
    Event midnight = EventBuilder.create()
        .withSubject("Midnight Release")
        .withStartDate(LocalDate.of(2025, 6, 1))
        .withStartTime(LocalTime.of(0, 0))
        .withEndTime(LocalTime.of(0, 30))
        .build();

    assertTrue(calendar.addEvent(midnight));

    // All-day event on same day as timed events
    Event allDay = EventBuilder.create()
        .withSubject("Birthday")
        .withStartDate(LocalDate.of(2025, 6, 1))
        .build();

    Event timedEvent = EventBuilder.create()
        .withSubject("Birthday Party")
        .withStartDate(LocalDate.of(2025, 6, 1))
        .withStartTime(LocalTime.of(18, 0))
        .withEndTime(LocalTime.of(22, 0))
        .build();

    assertTrue(calendar.addEvent(allDay));
    assertTrue(calendar.addEvent(timedEvent));

    List<Event> birthdayEvents = calendar.getEventsOnDate(LocalDate.of(2025, 6, 1));
    assertEquals(3, birthdayEvents.size()); // midnight, all-day, and party
  }

  @Test
  void testPerformanceWithManyEvents() {
    // Add many events
    for (int month = 1; month <= 12; month++) {
      for (int day = 1; day <= 28; day++) {
        Event event = EventBuilder.create()
            .withSubject("Event " + month + "-" + day)
            .withStartDate(LocalDate.of(2025, month, day))
            .withStartTime(LocalTime.of(10, 0))
            .withEndTime(LocalTime.of(11, 0))
            .build();

        calendar.addEvent(event);
      }
    }

    assertEquals(336, calendar.getAllEvents().size());

    // Query performance
    List<Event> januaryEvents = calendar.getEventsInRange(
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2025, 1, 31)
    );

    assertEquals(28, januaryEvents.size());

    // Export performance
    String csv = calendar.exportToCSV();
    assertNotNull(csv);

    String[] lines = csv.split("\n");
    assertEquals(337, lines.length); // Header + 336 events
  }
}