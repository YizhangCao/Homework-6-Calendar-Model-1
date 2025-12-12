package model.export;

import model.calendar.Calendar;

/**
 * Interface for exporting calendars to different formats.
 */
public interface CalendarExporter {

  /**
   * Exports a calendar to a string representation.
   */
  String export(Calendar calendar);
}