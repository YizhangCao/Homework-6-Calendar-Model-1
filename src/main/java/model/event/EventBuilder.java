// EventBuilder.java
package model.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Builder for creating and modifying events.
 */
public class EventBuilder {
  private String subject;
  private LocalDate startDate;
  private LocalTime startTime;
  private EventVisibility visibility = EventVisibility.PUBLIC;
  private String description;
  private LocalDate endDate;
  private LocalTime endTime;
  private String location;

  /**
   * Creates a new event builder.
   */
  public static EventBuilder create() {
    return new EventBuilder();
  }

  /**
   * Creates a builder from an existing event.
   */
  public static EventBuilder from(Event event) {
    EventBuilder builder = new EventBuilder();
    builder.subject = event.getSubject();
    builder.startDate = event.getStartDate();
    builder.startTime = event.getStartTime().orElse(null);
    builder.visibility = event.getVisibility();
    builder.description = event.getDescription().orElse(null);
    builder.endDate = event.getEndDate().orElse(null);
    builder.endTime = event.getEndTime().orElse(null);
    builder.location = event.getLocation().orElse(null);
    return builder;
  }

  /**
   * Sets the subject of the event.
   */
  public EventBuilder withSubject(String subject) {
    this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
    return this;
  }

  /**
   * Sets the start date of the event.
   */
  public EventBuilder withStartDate(LocalDate startDate) {
    this.startDate = Objects.requireNonNull(startDate, "Start date cannot be null");
    return this;
  }

  /**
   * Sets the start time of the event.
   */
  public EventBuilder withStartTime(LocalTime startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Sets the visibility of the event.
   */
  public EventBuilder withVisibility(EventVisibility visibility) {
    this.visibility = Objects.requireNonNull(visibility, "Visibility cannot be null");
    return this;
  }

  /**
   * Sets the description of the event.
   */
  public EventBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the end date of the event.
   */
  public EventBuilder withEndDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Sets the end time of the event.
   */
  public EventBuilder withEndTime(LocalTime endTime) {
    this.endTime = endTime;
    return this;
  }

  /**
   * Sets the location of the event.
   */
  public EventBuilder withLocation(String location) {
    this.location = location;
    return this;
  }

  /**
   * Builds a single event.
   */
  public SingleEvent build() {
    validate();
    return new SingleEvent(this);
  }

  /**
   * Validates the event configuration.
   */
  private void validate() {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalStateException("Subject is required");
    }
    if (startDate == null) {
      throw new IllegalStateException("Start date is required");
    }
    if (startTime == null && endTime != null) {
      throw new IllegalStateException("End time cannot be set without start time");
    }
    if (startTime != null && endDate == null) {
      endDate = startDate;
    }
    if (endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalStateException("End date cannot be before start date");
    }
    if (startTime != null && endTime != null && endDate != null && endDate.equals(startDate)
        && endTime.isBefore(startTime)) {
      throw new IllegalStateException("End time cannot be before start time on same day");
    }
  }

  // Change these methods from package-private to public
  public String getSubject() {
    return subject;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public EventVisibility getVisibility() {
    return visibility;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public String getLocation() {
    return location;
  }
}