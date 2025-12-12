package model.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Represents a calendar event.
 */
public interface Event {

  /**
   * Gets the subject of the event.
   */
  String getSubject();

  /**
   * Gets the start date of the event.
   */
  LocalDate getStartDate();

  /**
   * Gets the start time of the event.
   */
  Optional<LocalTime> getStartTime();

  /**
   * Gets the visibility of the event.
   */
  EventVisibility getVisibility();

  /**
   * Gets the description of the event.
   */
  Optional<String> getDescription();

  /**
   * Gets the end date of the event.
   */
  Optional<LocalDate> getEndDate();

  /**
   * Gets the end time of the event.
   */
  Optional<LocalTime> getEndTime();

  /**
   * Gets the location of the event.
   */
  Optional<String> getLocation();

  /**
   * Checks if this is an all-day event.
   */
  boolean isAllDay();

  /**
   * Gets the start datetime of the event.
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end datetime of the event.
   */
  LocalDateTime getEndDateTime();

  /**
   * Checks if this event conflicts with another event.
   */
  boolean conflictsWith(Event other);

  /**
   * Creates a copy of this event with updated fields.
   */
  Event withUpdates(EventBuilder builder);
}