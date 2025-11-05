// DateTimeUtil.java
package model.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utility class for date and time operations.
 */
public final class DateTimeUtil {

  private DateTimeUtil() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  /**
   * Checks if two time intervals overlap.
   */
  public static boolean overlaps(LocalDateTime start1, LocalDateTime end1,
      LocalDateTime start2, LocalDateTime end2) {
    // Two intervals overlap if one starts before the other ends
    return start1.isBefore(end2) && start2.isBefore(end1);
  }

  /**
   * Checks if two all-day events overlap.
   * This method handles multi-day all-day events properly.
   */
  public static boolean overlapsAllDay(LocalDate start1, LocalDate end1,
      LocalDate start2, LocalDate end2) {
    // Date ranges overlap if:
    // - start1 is not after end2 AND
    // - start2 is not after end1
    return !start1.isAfter(end2) && !start2.isAfter(end1);
  }

  /**
   * Checks if two all-day events on single days overlap.
   * @deprecated Use overlapsAllDay(LocalDate, LocalDate, LocalDate, LocalDate) instead
   */
  @Deprecated
  public static boolean overlapsAllDay(LocalDate date1, LocalDate date2) {
    return date1.equals(date2);
  }

  /**
   * Creates end datetime for all-day events.
   * Returns the last moment of the given day (23:59:59.999999999).
   */
  public static LocalDateTime getEndOfDay(LocalDate date) {
    return date.atTime(LocalTime.MAX);
  }

  /**
   * Creates start datetime for all-day events.
   * Returns the first moment of the given day (00:00:00).
   */
  public static LocalDateTime getStartOfDay(LocalDate date) {
    return date.atStartOfDay();
  }
}