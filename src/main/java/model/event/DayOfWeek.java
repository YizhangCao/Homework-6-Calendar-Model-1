package model.event;

/**
 * Represents days of the week for recurring events.
 */
public enum DayOfWeek {
  SUNDAY(1),
  MONDAY(2),
  TUESDAY(3),
  WEDNESDAY(4),
  THURSDAY(5),
  FRIDAY(6),
  SATURDAY(7);

  private final int calendarDay;

  DayOfWeek(int calendarDay) {
    this.calendarDay = calendarDay;
  }

  /**
   * Gets the calendar day value (1-7).
   */
  public int getCalendarDay() {
    return calendarDay;
  }
}