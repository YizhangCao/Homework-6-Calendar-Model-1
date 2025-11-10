package model.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecurringEventTest {

  private LocalDate startDate;
  private RecurrencePattern pattern;

  @BeforeEach
  void setUp() {
    startDate = LocalDate.of(2025, 1, 1);
  }

  @Test
  void testWeeklyRecurrenceByOccurrences() {
    pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        4
    );

    RecurringEvent recurring = new RecurringEvent(
        "Weekly Meeting",
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        EventVisibility.PUBLIC,
        "Team sync",
        "Conference Room",
        pattern,
        startDate
    );

    List<Event> instances = recurring.getInstances();
    assertEquals(4, instances.size());

    // Verify days are correct
    for (Event instance : instances) {
      int dayOfWeek = instance.getStartDate().getDayOfWeek().getValue();
      assertTrue(dayOfWeek == 1 || dayOfWeek == 3); // Monday or Wednesday
    }
  }

  @Test
  void testRecurrenceUntilDate() {
    LocalDate endDate = LocalDate.of(2025, 1, 31);
    pattern = RecurrencePattern.untilDate(
        Set.of(DayOfWeek.FRIDAY),
        endDate
    );

    RecurringEvent recurring = new RecurringEvent(
        "Friday Review",
        LocalTime.of(15, 0),
        LocalTime.of(16, 0),
        EventVisibility.PRIVATE,
        null,
        null,
        pattern,
        startDate
    );

    List<Event> instances = recurring.getInstances();

    // Verify all instances are Fridays
    for (Event instance : instances) {
      assertEquals(5, instance.getStartDate().getDayOfWeek().getValue());
      assertFalse(instance.getStartDate().isAfter(endDate));
    }
  }

  @Test
  void testModifySingleInstance() {
    pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.TUESDAY),
        3
    );

    RecurringEvent recurring = new RecurringEvent(
        "Tuesday Task",
        LocalTime.of(9, 0),
        LocalTime.of(10, 0),
        EventVisibility.PUBLIC,
        "Regular task",
        "Office",
        pattern,
        startDate
    );

    List<Event> instances = recurring.getInstances();
    LocalDate firstTuesday = instances.get(0).getStartDate();

    EventBuilder update = EventBuilder.create()
        .withSubject("Modified Task")
        .withStartDate(firstTuesday)
        .withStartTime(LocalTime.of(11, 0))
        .withEndTime(LocalTime.of(12, 0));

    recurring.modifyInstance(firstTuesday, update);

    // Verify modification
    Event modified = recurring.getInstance(firstTuesday).orElse(null);
    assertNotNull(modified);
    assertEquals("Modified Task", modified.getSubject());
    assertEquals(LocalTime.of(11, 0), modified.getStartTime().orElse(null));
  }

  @Test
  void testModifyAllInstances() {
    pattern = RecurrencePattern.withOccurrences(
        Set.of(DayOfWeek.MONDAY),
        3
    );

    RecurringEvent recurring = new RecurringEvent(
        "Monday Meeting",
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        EventVisibility.PUBLIC,
        "Team meeting",
        "Room A",
        pattern,
        startDate
    );

    EventBuilder update = EventBuilder.create()
        .withSubject("Updated Meeting")
        .withStartDate(startDate)
        .withStartTime(LocalTime.of(14, 0))
        .withEndTime(LocalTime.of(15, 0))
        .withLocation("Room B");

    recurring.modifyAll(update);

    // Verify all instances are modified
    for (Event instance : recurring.getInstances()) {
      assertEquals("Updated Meeting", instance.getSubject());
      assertEquals("Room B", instance.getLocation().orElse(""));
    }
  }
}