package model.calendar;

import model.event.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CalendarImplTest {
  private CalendarImpl calendar;
  private LocalDate testDate;

  @BeforeEach
  void setUp() {
    calendar = new CalendarImpl("Test Calendar");
    testDate = LocalDate.of(2025, 6, 15);
  }

  @Test
  void testCalendarCreation() {
    assertEquals("Test Calendar", calendar.getTitle());
    assertEquals(ConflictPolicy.REJECT_CONFLICTS, calendar.getConflictPolicy());
  }

  @ParameterizedTest
  @EnumSource(ConflictPolicy.class)
  void testConflictPolicies(ConflictPolicy policy) {
    calendar = new CalendarImpl("Test", policy);
    assertEquals(policy, calendar.getConflictPolicy());
  }

  @Test
  void testAddEvent() {
    Event event = EventBuilder.create()
        .withSubject("Test Event")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    assertTrue(calendar.addEvent(event));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  void testPreventDuplicateEvents() {
    Event event1 = EventBuilder.create()
        .withSubject("Meeting")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Meeting")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .withDescription("Different description")
        .build();

    assertTrue(calendar.addEvent(event1));
    assertFalse(calendar.addEvent(event2));
  }

  @Test
  void testConflictDetectionWithRejectPolicy() {
    Event event1 = EventBuilder.create()
        .withSubject("Event 1")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(12, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Event 2")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(11, 0))
        .withEndTime(LocalTime.of(13, 0))
        .build();

    assertTrue(calendar.addEvent(event1));
    assertFalse(calendar.addEvent(event2));
  }

  @Test
  void testConflictAllowedWithAllowPolicy() {
    calendar = new CalendarImpl("Test", ConflictPolicy.ALLOW_CONFLICTS);

    Event event1 = EventBuilder.create()
        .withSubject("Event 1")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(12, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Event 2")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(11, 0))
        .withEndTime(LocalTime.of(13, 0))
        .build();

    assertTrue(calendar.addEvent(event1));
    assertTrue(calendar.addEvent(event2));
  }

  @Test
  void testGetEvent() {
    Event event = EventBuilder.create()
        .withSubject("Find Me")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(14, 30))
        .build();

    calendar.addEvent(event);

    assertTrue(calendar.getEvent("Find Me", testDate, LocalTime.of(14, 30)).isPresent());
    assertFalse(calendar.getEvent("Find Me", testDate, LocalTime.of(15, 0)).isPresent());
    assertFalse(calendar.getEvent("Wrong", testDate, LocalTime.of(14, 30)).isPresent());
  }

  @Test
  void testGetEventsOnDate() {
    Event event1 = EventBuilder.create()
        .withSubject("Morning")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(9, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Afternoon")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(14, 0))
        .build();

    Event event3 = EventBuilder.create()
        .withSubject("Different Day")
        .withStartDate(testDate.plusDays(1))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    List<Event> eventsOnDate = calendar.getEventsOnDate(testDate);
    assertEquals(2, eventsOnDate.size());
  }

  @Test
  void testGetEventsInRange() {
    LocalDate start = LocalDate.of(2025, 1, 1);
    LocalDate end = LocalDate.of(2025, 1, 31);

    Event event1 = EventBuilder.create()
        .withSubject("New Year")
        .withStartDate(start)
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Mid January")
        .withStartDate(LocalDate.of(2025, 1, 15))
        .build();

    Event event3 = EventBuilder.create()
        .withSubject("February")
        .withStartDate(LocalDate.of(2025, 2, 1))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    List<Event> eventsInRange = calendar.getEventsInRange(start, end);
    assertEquals(2, eventsInRange.size());
  }

  @Test
  void testIsBusy() {
    Event event = EventBuilder.create()
        .withSubject("Busy Time")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(12, 0))
        .build();

    calendar.addEvent(event);

    assertTrue(calendar.isBusy(testDate, LocalTime.of(10, 30)));
    assertTrue(calendar.isBusy(testDate, LocalTime.of(11, 59)));
    assertFalse(calendar.isBusy(testDate, LocalTime.of(12, 0)));
    assertFalse(calendar.isBusy(testDate, LocalTime.of(9, 59)));
  }

  @Test
  void testUpdateEvent() {
    Event original = EventBuilder.create()
        .withSubject("Original")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    calendar.addEvent(original);

    EventBuilder updates = EventBuilder.from(original)
        .withDescription("Updated description")
        .withEndTime(LocalTime.of(11, 30));

    assertTrue(calendar.updateEvent(original, updates));

    Event updated = calendar.getEvent("Original", testDate, LocalTime.of(10, 0))
        .orElse(null);
    assertNotNull(updated);
    assertEquals("Updated description", updated.getDescription().orElse(""));
  }

  @Test
  void testAddRecurringEvent() {
    EventBuilder base = EventBuilder.create()
        .withSubject("Weekly Meeting")
        .withStartDate(LocalDate.of(2025, 1, 6)) // Monday
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0));

    RecurrencePattern pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
        4
    );

    RecurringEvent recurring = calendar.addRecurringEvent(
        base,
        pattern,
        LocalDate.of(2025, 1, 6)
    );

    assertNotNull(recurring);
    assertEquals(4, recurring.getInstances().size());
  }

  @Test
  void testRecurringEventConflictDetection() {
    // Add a single event
    Event blocking = EventBuilder.create()
        .withSubject("Blocking Event")
        .withStartDate(LocalDate.of(2025, 1, 10)) // Friday
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(blocking);

    // Try to add recurring event that conflicts
    EventBuilder base = EventBuilder.create()
        .withSubject("Recurring")
        .withStartDate(LocalDate.of(2025, 1, 6))
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0));

    RecurrencePattern pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.FRIDAY),
        2
    );

    assertThrows(IllegalArgumentException.class, () ->
        calendar.addRecurringEvent(base, pattern, LocalDate.of(2025, 1, 6))
    );
  }
}
