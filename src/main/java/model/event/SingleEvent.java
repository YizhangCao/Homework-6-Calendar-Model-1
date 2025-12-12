// SingleEvent.java
package model.event;

import model.util.DateTimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single, non-recurring event.
 */
public class SingleEvent implements Event {
  private final String subject;
  private final LocalDate startDate;
  private final LocalTime startTime;
  private final EventVisibility visibility;
  private final String description;
  private final LocalDate endDate;
  private final LocalTime endTime;
  private final String location;

  /**
   * Creates a single event from a builder.
   */
  SingleEvent(EventBuilder builder) {
    this.subject = builder.getSubject();
    this.startDate = builder.getStartDate();
    this.startTime = builder.getStartTime();
    this.visibility = builder.getVisibility();
    this.description = builder.getDescription();
    this.endDate = builder.getEndDate();
    this.endTime = builder.getEndTime();
    this.location = builder.getLocation();
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDate getStartDate() {
    return startDate;
  }

  @Override
  public Optional<LocalTime> getStartTime() {
    return Optional.ofNullable(startTime);
  }

  @Override
  public EventVisibility getVisibility() {
    return visibility;
  }

  @Override
  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  @Override
  public Optional<LocalDate> getEndDate() {
    return Optional.ofNullable(endDate);
  }

  @Override
  public Optional<LocalTime> getEndTime() {
    return Optional.ofNullable(endTime);
  }

  @Override
  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  @Override
  public boolean isAllDay() {
    return startTime == null;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startTime != null
        ? LocalDateTime.of(startDate, startTime)
        : startDate.atStartOfDay();
  }

  @Override
  public LocalDateTime getEndDateTime() {
    if (endDate != null && endTime != null) {
      return LocalDateTime.of(endDate, endTime);
    } else if (startTime != null && endTime != null) {
      // Start time and end time on same day
      return LocalDateTime.of(startDate, endTime);
    } else if (startTime != null) {
      // Timed event without end time - assume 1 hour duration
      LocalDate effectiveEndDate = endDate != null ? endDate : startDate;
      return LocalDateTime.of(effectiveEndDate, startTime.plusHours(1));
    } else if (endDate != null) {
      // All-day event spanning multiple days
      return DateTimeUtil.getEndOfDay(endDate);
    } else {
      // All-day event on single day
      return DateTimeUtil.getEndOfDay(startDate);
    }
  }

  @Override
  public boolean conflictsWith(Event other) {
    // All-day events do not conflict with timed events
    if (this.isAllDay() != other.isAllDay()) {
      return false;
    }

    // Both are all-day events - check if dates overlap
    if (this.isAllDay() && other.isAllDay()) {
      LocalDate thisEnd = this.endDate != null ? this.endDate : this.startDate;
      LocalDate otherEnd = other.getEndDate().orElse(other.getStartDate());

      // Check if date ranges overlap
      return !this.startDate.isAfter(otherEnd) && !thisEnd.isBefore(other.getStartDate());
    }

    // Both are timed events - use datetime comparison
    return DateTimeUtil.overlaps(
        this.getStartDateTime(),
        this.getEndDateTime(),
        other.getStartDateTime(),
        other.getEndDateTime()
    );
  }

  @Override
  public Event withUpdates(EventBuilder builder) {
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SingleEvent)) return false;
    SingleEvent that = (SingleEvent) o;
    return Objects.equals(subject, that.subject)
        && Objects.equals(startDate, that.startDate)
        && Objects.equals(startTime, that.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, startTime);
  }

  @Override
  public String toString() {
    return String.format("Event[subject=%s, date=%s, time=%s]",
        subject, startDate, startTime != null ? startTime : "all-day");
  }
}