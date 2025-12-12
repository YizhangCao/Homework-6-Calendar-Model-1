package model.calendar;

import model.event.Event;

/**
 * Interface for listening to calendar event changes.
 * Implementations of this interface can be registered with a Calendar
 * to receive notifications when events are added or modified.
 */
public interface CalendarListener {

  /**
   * Called when an event is added to the calendar.
   *
   * @param event the event that was added
   */
  void onEventAdded(Event event);

  /**
   * Called when an event is modified in the calendar.
   *
   * @param event the event that was modified
   */
  void onEventModified(Event event);
}