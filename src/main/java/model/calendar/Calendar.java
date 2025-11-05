package model.calendar;

import model.event.Event;
import model.event.EventBuilder;
import model.event.RecurrencePattern;
import model.event.RecurringEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents a calendar that can hold events.
 */
public interface Calendar {

  /**
   * Gets the title of the calendar.
   */
  String getTitle();

  /**
   * Gets the conflict policy of the calendar.
   */
  ConflictPolicy getConflictPolicy();

  /**
   * Adds a single event to the calendar.
   */
  boolean addEvent(Event event);

  /**
   * Creates and adds a recurring event to the calendar.
   */
  RecurringEvent addRecurringEvent(EventBuilder baseEvent,
      RecurrencePattern pattern,
      LocalDate startDate);

  /**
   * Retrieves an event by subject, date, and time.
   */
  Optional<Event> getEvent(String subject, LocalDate date, LocalTime time);

  /**
   * Retrieves all events on a specific date.
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Retrieves all events in a date range.
   */
  List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

  List<Event> getAllEvents();

  /**
   * Checks if the user is busy at a specific date and time.
   */
  boolean isBusy(LocalDate date, LocalTime time);

  /**
   * Updates an existing event.
   */
  boolean updateEvent(Event event, EventBuilder updates);

  /**
   * Exports the calendar to CSV format.
   */
  String exportToCSV();
}