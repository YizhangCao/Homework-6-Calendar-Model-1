package model.event;

import java.time.LocalDate;
import java.util.Set;
import java.util.Objects;

/**
 * Represents the recurrence pattern for recurring events.
 */
public class RecurrencePattern {
  private final Set<DayOfWeek> daysOfWeek;
  private final Integer occurrences;
  private final LocalDate endDate;

  /**
   * Creates a recurrence pattern with a specific number of occurrences.
   */
  public static RecurrencePattern withOccurrences(Set<DayOfWeek> daysOfWeek, int occurrences) {
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    return new RecurrencePattern(daysOfWeek, occurrences, null);
  }

  /**
   * Creates a recurrence pattern until a specific end date.
   */
  public static RecurrencePattern untilDate(Set<DayOfWeek> daysOfWeek, LocalDate endDate) {
    Objects.requireNonNull(endDate, "End date cannot be null");
    return new RecurrencePattern(daysOfWeek, null, endDate);
  }

  private RecurrencePattern(Set<DayOfWeek> daysOfWeek, Integer occurrences, LocalDate endDate) {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Days of week cannot be empty");
    }
    this.daysOfWeek = Set.copyOf(daysOfWeek);
    this.occurrences = occurrences;
    this.endDate = endDate;
  }

  /**
   * Gets the days of the week for recurrence.
   */
  public Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Gets the number of occurrences.
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the end date for recurrence.
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Checks if recurrence is based on occurrences.
   */
  public boolean isOccurrenceBased() {
    return occurrences != null;
  }
}