package model.event;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class EventBuilderTest {
  private EventBuilder builder;

  @BeforeEach
  void setUp() {
    builder = EventBuilder.create();
  }

  @Test
  void testCreateValidEvent() {
    Event event = builder
        .withSubject("Meeting")
        .withStartDate(LocalDate.of(2025, 1, 15))
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .build();

    assertNotNull(event);
    assertEquals("Meeting", event.getSubject());
    assertEquals(LocalDate.of(2025, 1, 15), event.getStartDate());
    assertTrue(event.getStartTime().isPresent());
    assertEquals(LocalTime.of(10, 0), event.getStartTime().get());
  }

  @Test
  void testCreateAllDayEvent() {
    Event event = builder
        .withSubject("Holiday")
        .withStartDate(LocalDate.of(2025, 12, 25))
        .build();

    assertTrue(event.isAllDay());
    assertFalse(event.getStartTime().isPresent());
  }

  @Test
  void testMissingRequiredFields() {
    assertThrows(IllegalStateException.class, () -> builder.build());

    builder.withSubject("Test");
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void testInvalidTimeConfiguration() {
    builder
        .withSubject("Invalid")
        .withStartDate(LocalDate.of(2025, 1, 1))
        .withEndTime(LocalTime.of(10, 0));

    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void testEndDateBeforeStartDate() {
    builder
        .withSubject("Invalid")
        .withStartDate(LocalDate.of(2025, 1, 15))
        .withStartTime(LocalTime.of(10, 0))
        .withEndDate(LocalDate.of(2025, 1, 14));

    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @ParameterizedTest
  @MethodSource("visibilityProvider")
  void testVisibilitySettings(EventVisibility visibility) {
    Event event = builder
        .withSubject("Test")
        .withStartDate(LocalDate.of(2025, 1, 1))
        .withVisibility(visibility)
        .build();

    assertEquals(visibility, event.getVisibility());
  }

  static Stream<Arguments> visibilityProvider() {
    return Stream.of(
        Arguments.of(EventVisibility.PUBLIC),
        Arguments.of(EventVisibility.PRIVATE)
    );
  }
}
