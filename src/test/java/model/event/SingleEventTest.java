package model.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class SingleEventTest {
  private EventBuilder builder;

  @BeforeEach
  void setUp() {
    builder = EventBuilder.create()
        .withSubject("Test Event")
        .withStartDate(LocalDate.of(2025, 3, 15));
  }

  @Test
  void testEventEquality() {
    Event event1 = builder
        .withStartTime(LocalTime.of(10, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Test Event")
        .withStartDate(LocalDate.of(2025, 3, 15))
        .withStartTime(LocalTime.of(10, 0))
        .build();

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void testEventInequality() {
    Event event1 = builder.withStartTime(LocalTime.of(10, 0)).build();
    Event event2 = builder.withStartTime(LocalTime.of(11, 0)).build();

    assertNotEquals(event1, event2);
  }

  @Test
  void testConflictDetection() {
    Event event1 = builder
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(12, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Another Event")
        .withStartDate(LocalDate.of(2025, 3, 15))
        .withStartTime(LocalTime.of(11, 0))
        .withEndTime(LocalTime.of(13, 0))
        .build();

    assertTrue(event1.conflictsWith(event2));
    assertTrue(event2.conflictsWith(event1));
  }

  @Test
  void testNoConflict() {
    Event event1 = builder
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Another Event")
        .withStartDate(LocalDate.of(2025, 3, 15))
        .withStartTime(LocalTime.of(11, 0))
        .withEndTime(LocalTime.of(12, 0))
        .build();

    assertFalse(event1.conflictsWith(event2));
  }

  @Test
  void testAllDayEventConflict() {
    Event allDay1 = builder.build();
    Event allDay2 = EventBuilder.create()
        .withSubject("Another Holiday")
        .withStartDate(LocalDate.of(2025, 3, 15))
        .build();

    assertTrue(allDay1.conflictsWith(allDay2));
  }

  @Test
  void testEventUpdate() {
    Event original = builder
        .withStartTime(LocalTime.of(10, 0))
        .withDescription("Original description")
        .build();

    EventBuilder updateBuilder = EventBuilder.from(original)
        .withDescription("Updated description")
        .withEndTime(LocalTime.of(12, 0));

    Event updated = original.withUpdates(updateBuilder);

    assertEquals("Updated description", updated.getDescription().orElse(""));
    assertEquals(LocalTime.of(12, 0), updated.getEndTime().orElse(null));
  }
}