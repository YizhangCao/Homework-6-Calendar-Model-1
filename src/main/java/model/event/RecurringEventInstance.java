package model.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Objects;

/**
 * Represents a single instance of a recurring event.
 */
public class RecurringEventInstance implements Event {

  private final RecurringEvent parent;
  private final LocalDate date;
  private boolean isModified;
  private SingleEvent modifiedEvent;

  /**
   * Creates an instance of a recurring event.
   */
  public RecurringEventInstance(RecurringEvent parent, LocalDate date, boolean isModified) {
    this.parent = Objects.requireNonNull(parent);
    this.date = Objects.requireNonNull(date);
    this.isModified = isModified;
  }

  /**
   * Modifies this instance.
   */
  public void modify(EventBuilder updates) {
    this.modifiedEvent = updates.build();
    this.isModified = true;
  }

  @Override
  public String getSubject() {
    return isModified && modifiedEvent != null
        ? modifiedEvent.getSubject()
        : parent.getSubject();
  }

  @Override
  public LocalDate getStartDate() {
    return isModified && modifiedEvent != null
        ? modifiedEvent.getStartDate()
        : date;
  }

  @Override
  public Optional<LocalTime> getStartTime() {
    if (isModified && modifiedEvent != null) {
      return modifiedEvent.getStartTime();
    }
    return Optional.of(parent.getStartTime());
  }

  @Override
  public EventVisibility getVisibility() {
    return isModified && modifiedEvent != null
        ? modifiedEvent.getVisibility()
        : parent.getVisibility();
  }

  @Override
  public Optional<String> getDescription() {
    if (isModified && modifiedEvent != null) {
      return modifiedEvent.getDescription();
    }
    return Optional.ofNullable(parent.getDescription());
  }

  @Override
  public Optional<LocalDate> getEndDate() {
    if (isModified && modifiedEvent != null) {
      return modifiedEvent.getEndDate();
    }
    return Optional.of(date);
  }

  @Override
  public Optional<LocalTime> getEndTime() {
    if (isModified && modifiedEvent != null) {
      return modifiedEvent.getEndTime();
    }
    return Optional.ofNullable(parent.getEndTime());
  }

  @Override
  public Optional<String> getLocation() {
    if (isModified && modifiedEvent != null) {
      return modifiedEvent.getLocation();
    }
    return Optional.ofNullable(parent.getLocation());
  }

  @Override
  public boolean isAllDay() {
    return false; // Recurring events always have times
  }

  @Override
  public LocalDateTime getStartDateTime() {
    LocalDate actualDate = getStartDate();
    LocalTime actualTime = getStartTime().orElse(LocalTime.MIDNIGHT);
    return LocalDateTime.of(actualDate, actualTime);
  }

  @Override
  public LocalDateTime getEndDateTime() {
    LocalDate actualEndDate = getEndDate().orElse(getStartDate());
    LocalTime actualEndTime = getEndTime().orElse(
        getStartTime().orElse(LocalTime.MIDNIGHT).plusHours(1)
    );
    return LocalDateTime.of(actualEndDate, actualEndTime);
  }

  @Override
  public boolean conflictsWith(Event other) {
    return other.getStartDateTime().isBefore(this.getEndDateTime())
        && this.getStartDateTime().isBefore(other.getEndDateTime());
  }

  @Override
  public Event withUpdates(EventBuilder builder) {
    modify(builder);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof RecurringEventInstance))
      return false;
    RecurringEventInstance that = (RecurringEventInstance) o;
    return Objects.equals(getSubject(), that.getSubject())
        && Objects.equals(getStartDate(), that.getStartDate())
        && Objects.equals(getStartTime(), that.getStartTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSubject(), getStartDate(), getStartTime());
  }
}