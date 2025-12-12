package model.calendar;

import model.event.Event;
import model.event.EventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CalendarListener observer pattern implementation.
 * These tests verify that listeners are properly notified when events
 * are added or modified, and that unregistered listeners do not receive
 * notifications.
 */
class CalendarListenerTest {

  private CalendarImpl calendar;
  private TestListener listener1;
  private TestListener listener2;
  private LocalDate testDate;

  /**
   * A test implementation of CalendarListener that records all notifications.
   */
  private static class TestListener implements CalendarListener {
    private final List<Event> addedEvents = new ArrayList<>();
    private final List<Event> modifiedEvents = new ArrayList<>();

    @Override
    public void onEventAdded(Event event) {
      addedEvents.add(event);
    }

    @Override
    public void onEventModified(Event event) {
      modifiedEvents.add(event);
    }

    public List<Event> getAddedEvents() {
      return new ArrayList<>(addedEvents);
    }

    public List<Event> getModifiedEvents() {
      return new ArrayList<>(modifiedEvents);
    }

    public void clear() {
      addedEvents.clear();
      modifiedEvents.clear();
    }
  }

  @BeforeEach
  void setUp() {
    calendar = new CalendarImpl("Test Calendar");
    listener1 = new TestListener();
    listener2 = new TestListener();
    testDate = LocalDate.of(2025, 6, 15);
  }

  @Test
  void testListenerReceivesAddedEventNotification() {
    calendar.addCalendarListener(listener1);

    Event event = EventBuilder.create()
        .withSubject("Meeting")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    calendar.addEvent(event);

    assertEquals(1, listener1.getAddedEvents().size());
    assertEquals("Meeting", listener1.getAddedEvents().get(0).getSubject());
  }

  @Test
  void testMultipleListenersReceiveNotifications() {
    calendar.addCalendarListener(listener1);
    calendar.addCalendarListener(listener2);

    Event event = EventBuilder.create()
        .withSubject("Team Standup")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(9, 0))
        .build();

    calendar.addEvent(event);

    // Both listeners should receive the notification
    assertEquals(1, listener1.getAddedEvents().size());
    assertEquals(1, listener2.getAddedEvents().size());
    assertEquals("Team Standup", listener1.getAddedEvents().get(0).getSubject());
    assertEquals("Team Standup", listener2.getAddedEvents().get(0).getSubject());
  }

  @Test
  void testRemovedListenerDoesNotReceiveNotifications() {
    calendar.addCalendarListener(listener1);
    calendar.addCalendarListener(listener2);

    // Remove listener1
    calendar.removeCalendarListener(listener1);

    Event event = EventBuilder.create()
        .withSubject("Private Meeting")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(14, 0))
        .build();

    calendar.addEvent(event);

    // listener1 should NOT receive notification
    assertEquals(0, listener1.getAddedEvents().size());
    // listener2 should still receive notification
    assertEquals(1, listener2.getAddedEvents().size());
  }

  @Test
  void testListenerReceivesModifiedEventNotification() {
    calendar.addCalendarListener(listener1);

    Event original = EventBuilder.create()
        .withSubject("Original Event")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    calendar.addEvent(original);
    listener1.clear(); // Clear the add notification

    EventBuilder updates = EventBuilder.from(original)
        .withDescription("Updated description");

    calendar.updateEvent(original, updates);

    assertEquals(1, listener1.getModifiedEvents().size());
  }

  @Test
  void testUnregisteredListenerReceivesNoNotifications() {
    // Do NOT register listener1
    Event event = EventBuilder.create()
        .withSubject("Secret Event")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(12, 0))
        .build();

    calendar.addEvent(event);

    // Unregistered listener should receive nothing
    assertEquals(0, listener1.getAddedEvents().size());
    assertEquals(0, listener1.getModifiedEvents().size());
  }

  @Test
  void testAddingSameListenerTwiceOnlyNotifiesOnce() {
    calendar.addCalendarListener(listener1);
    calendar.addCalendarListener(listener1); // Add same listener again

    Event event = EventBuilder.create()
        .withSubject("Duplicate Test")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);

    // Should only receive one notification, not two
    assertEquals(1, listener1.getAddedEvents().size());
  }

  @Test
  void testRemovingNonExistentListenerDoesNotThrow() {
    // Should not throw when removing a listener that was never added
    assertDoesNotThrow(() -> calendar.removeCalendarListener(listener1));
  }

  @Test
  void testAddingNullListenerDoesNotThrow() {
    // Should not throw when adding null listener
    assertDoesNotThrow(() -> calendar.addCalendarListener(null));
  }

  @Test
  void testMultipleEventsNotifyListenerMultipleTimes() {
    calendar.addCalendarListener(listener1);

    Event event1 = EventBuilder.create()
        .withSubject("Event 1")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(9, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Event 2")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    Event event3 = EventBuilder.create()
        .withSubject("Event 3")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    assertEquals(3, listener1.getAddedEvents().size());
  }

  @Test
  void testListenerOnlyReceivesNotificationsFromItsCalendar() {
    CalendarImpl calendar2 = new CalendarImpl("Second Calendar");

    calendar.addCalendarListener(listener1);
    calendar2.addCalendarListener(listener2);

    Event event1 = EventBuilder.create()
        .withSubject("Calendar 1 Event")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(10, 0))
        .build();

    Event event2 = EventBuilder.create()
        .withSubject("Calendar 2 Event")
        .withStartDate(testDate)
        .withStartTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event1);
    calendar2.addEvent(event2);

    // listener1 should only see event from calendar1
    assertEquals(1, listener1.getAddedEvents().size());
    assertEquals("Calendar 1 Event", listener1.getAddedEvents().get(0).getSubject());

    // listener2 should only see event from calendar2
    assertEquals(1, listener2.getAddedEvents().size());
    assertEquals("Calendar 2 Event", listener2.getAddedEvents().get(0).getSubject());
  }
}
