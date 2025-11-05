package model.calendar;

import model.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of a calendar that manages events.
 * Supports single and recurring events with conflict detection.
 */
public class CalendarImpl implements Calendar {
  private final String title;
  private final ConflictPolicy conflictPolicy;
  private final List<Event> events;
  private final List<RecurringEvent> recurringEvents;

  /**
   * Creates a calendar with the default conflict policy (REJECT_CONFLICTS).
   *
   * @param title the title of the calendar
   * @throws IllegalArgumentException if title is null or empty
   */
  public CalendarImpl(String title) {
    this(title, ConflictPolicy.REJECT_CONFLICTS);
  }

  /**
   * Creates a calendar with the specified conflict policy.
   *
   * @param title the title of the calendar
   * @param conflictPolicy the policy for handling conflicting events
   * @throws IllegalArgumentException if title is null or empty, or if conflictPolicy is null
   */
  public CalendarImpl(String title, ConflictPolicy conflictPolicy) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar title cannot be null or empty");
    }
    if (conflictPolicy == null) {
      throw new IllegalArgumentException("Conflict policy cannot be null");
    }
    this.title = title;
    this.conflictPolicy = conflictPolicy;
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
  }

  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Gets the conflict policy for this calendar.
   *
   * @return the conflict policy
   */
  public ConflictPolicy getConflictPolicy() {
    return conflictPolicy;
  }

  @Override
  public boolean addEvent(Event event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    // Check for duplicate events (same subject, date, and start time)
    for (Event existing : events) {
      if (isDuplicateEvent(existing, event)) {
        return false;
      }
    }

    // Check for conflicts if policy requires
    if (conflictPolicy == ConflictPolicy.REJECT_CONFLICTS) {
      if (hasConflict(event)) {
        return false;
      }
    }

    return events.add(event);
  }

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   */
  public void removeEvent(Event event) {
    events.remove(event);
  }

  @Override
  public boolean updateEvent(Event originalEvent, EventBuilder updates) {
    if (originalEvent == null || updates == null) {
      throw new IllegalArgumentException("Original event and updates cannot be null");
    }

    if (!events.contains(originalEvent)) {
      return false;
    }

    // Build the updated event
    Event updatedEvent = updates.build();

    // Check for conflicts with the updated event (excluding the original)
    if (conflictPolicy == ConflictPolicy.REJECT_CONFLICTS) {
      List<Event> tempEvents = new ArrayList<>(events);
      tempEvents.remove(originalEvent);

      for (Event existing : tempEvents) {
        if (existing.conflictsWith(updatedEvent)) {
          return false;
        }
      }
    }

    // Replace the event
    int index = events.indexOf(originalEvent);
    events.set(index, updatedEvent);
    return true;
  }

  @Override
  public Optional<Event> getEvent(String subject, LocalDate date, LocalTime time) {
    if (subject == null || date == null) {
      return Optional.empty();
    }

    return events.stream()
        .filter(e -> e.getSubject().equals(subject))
        .filter(e -> e.getStartDate().equals(date))
        .filter(e -> {
          Optional<LocalTime> startTime = e.getStartTime();
          if (time == null) {
            return startTime.isEmpty();
          } else {
            return startTime.isPresent() && startTime.get().equals(time);
          }
        })
        .findFirst();
  }

  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    List<Event> eventsOnDate = new ArrayList<>();

    // Add single events that are active on this date
    for (Event event : events) {
      if (isEventActiveOnDate(event, date)) {
        eventsOnDate.add(event);
      }
    }

    // Add recurring event instances for this date
    for (RecurringEvent recurring : recurringEvents) {
      Optional<RecurringEventInstance> instance = recurring.getInstance(date);
      instance.ifPresent(eventsOnDate::add);
    }

    return eventsOnDate;
  }

  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must not be after end date");
    }

    List<Event> eventsInRange = new ArrayList<>();

    // Add single events that overlap with the range
    for (Event event : events) {
      if (isEventInRange(event, startDate, endDate)) {
        eventsInRange.add(event);
      }
    }

    // Add recurring event instances in the range
    for (RecurringEvent recurring : recurringEvents) {
      LocalDate current = startDate;
      while (!current.isAfter(endDate)) {
        Optional<RecurringEventInstance> instance = recurring.getInstance(current);
        instance.ifPresent(eventsInRange::add);
        current = current.plusDays(1);
      }
    }

    // Remove duplicates and sort
    return eventsInRange.stream()
        .distinct()
        .sorted(Comparator.comparing(Event::getStartDate)
            .thenComparing(e -> e.getStartTime().orElse(LocalTime.MIN)))
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getAllEvents() {
    List<Event> allEvents = new ArrayList<>(events);

    // Add all recurring event instances
    for (RecurringEvent recurring : recurringEvents) {
      List<Event> instances = recurring.getInstances();
      allEvents.addAll(instances);
    }

    return allEvents;
  }

  @Override
  public boolean isBusy(LocalDate date, LocalTime time) {
    if (date == null || time == null) {
      throw new IllegalArgumentException("Date and time cannot be null");
    }

    // Check single events
    for (Event event : events) {
      if (isTimeWithinEvent(event, date, time)) {
        return true;
      }
    }

    // Check recurring event instances
    for (RecurringEvent recurring : recurringEvents) {
      Optional<RecurringEventInstance> instance = recurring.getInstance(date);
      if (instance.isPresent() && isTimeWithinEvent(instance.get(), date, time)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public RecurringEvent addRecurringEvent(EventBuilder eventBuilder,
      RecurrencePattern pattern,
      LocalDate startDate) {
    if (eventBuilder == null || pattern == null || startDate == null) {
      throw new IllegalArgumentException("Parameters cannot be null");
    }

    // Build a base event to extract properties
    Event baseEvent = eventBuilder.build();

    // Create the recurring event with all required parameters
    RecurringEvent recurringEvent = new RecurringEvent(
        baseEvent.getSubject(),
        baseEvent.getStartTime().orElse(LocalTime.of(9, 0)),
        baseEvent.getEndTime().orElse(LocalTime.of(10, 0)),
        baseEvent.getVisibility(),
        baseEvent.getDescription().orElse(""),
        baseEvent.getLocation().orElse(""),
        pattern,
        startDate
    );

    // Check for conflicts with all instances if policy requires
    if (conflictPolicy == ConflictPolicy.REJECT_CONFLICTS) {
      List<Event> instances = recurringEvent.getInstances();
      for (Event instance : instances) {
        if (hasConflict(instance)) {
          throw new IllegalArgumentException(
              "Recurring event conflicts with existing event on " + instance.getStartDate()
          );
        }
      }
    }

    recurringEvents.add(recurringEvent);
    return recurringEvent;
  }

  @Override
  public String exportToCSV() {
    // AI-generated code for CSV export
    StringBuilder csv = new StringBuilder();

    // CSV Header (Google Calendar format)
    csv.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,")
        .append("Description,Location,Private\n");

    // Add all single events
    for (Event event : events) {
      appendEventToCSV(csv, event);
    }

    // Add all recurring event instances
    for (RecurringEvent recurring : recurringEvents) {
      for (Event instance : recurring.getInstances()) {
        appendEventToCSV(csv, instance);
      }
    }

    return csv.toString();
  }

  /**
   * Appends an event to the CSV StringBuilder.
   * AI-generated helper method for CSV export.
   */
  private void appendEventToCSV(StringBuilder csv, Event event) {
    csv.append("\"").append(escapeCSV(event.getSubject())).append("\",");
    csv.append(event.getStartDate()).append(",");

    if (event.isAllDay()) {
      csv.append(",,");  // No start/end time for all-day events
      csv.append(event.getEndDate().orElse(event.getStartDate())).append(",");
      csv.append(",TRUE,");  // All day event = TRUE
    } else {
      csv.append(event.getStartTime().orElse(LocalTime.MIDNIGHT)).append(",");
      csv.append(event.getEndDate().orElse(event.getStartDate())).append(",");
      csv.append(event.getEndTime()
              .orElse(event.getStartTime().orElse(LocalTime.MIDNIGHT).plusHours(1)))
          .append(",");
      csv.append("FALSE,");  // All day event = FALSE
    }

    csv.append("\"").append(escapeCSV(event.getDescription().orElse(""))).append("\",");
    csv.append("\"").append(escapeCSV(event.getLocation().orElse(""))).append("\",");
    csv.append(event.getVisibility() == EventVisibility.PRIVATE ? "TRUE" : "FALSE");
    csv.append("\n");
  }

  /**
   * Escapes special characters in CSV fields.
   */
  private String escapeCSV(String field) {
    return field.replace("\"", "\"\"");
  }

  /**
   * Checks if an event is active on a specific date.
   */
  private boolean isEventActiveOnDate(Event event, LocalDate date) {
    LocalDate startDate = event.getStartDate();
    LocalDate endDate = event.getEndDate().orElse(startDate);

    // Event is active if date falls within [startDate, endDate] inclusive
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * Checks if an event falls within a date range.
   */
  private boolean isEventInRange(Event event, LocalDate rangeStart, LocalDate rangeEnd) {
    LocalDate eventStart = event.getStartDate();
    LocalDate eventEnd = event.getEndDate().orElse(eventStart);

    // Event is in range if it overlaps with the range at all
    return !eventEnd.isBefore(rangeStart) && !eventStart.isAfter(rangeEnd);
  }

  /**
   * Checks if a specific time falls within an event on a given date.
   */
  private boolean isTimeWithinEvent(Event event, LocalDate date, LocalTime time) {
    if (!isEventActiveOnDate(event, date)) {
      return false;
    }

    // All-day events make any time on that date busy
    if (event.isAllDay()) {
      return true;
    }

    Optional<LocalTime> startTimeOpt = event.getStartTime();
    Optional<LocalTime> endTimeOpt = event.getEndTime();

    if (startTimeOpt.isEmpty()) {
      return false;
    }

    LocalTime startTime = startTimeOpt.get();
    LocalTime endTime = endTimeOpt.orElse(startTime.plusHours(1));

    // For multi-day events, check the specific day
    LocalDate eventStart = event.getStartDate();
    LocalDate eventEnd = event.getEndDate().orElse(eventStart);

    if (date.equals(eventStart) && date.equals(eventEnd)) {
      // Single day event
      return !time.isBefore(startTime) && time.isBefore(endTime);
    } else if (date.equals(eventStart)) {
      // First day of multi-day event
      return !time.isBefore(startTime);
    } else if (date.equals(eventEnd)) {
      // Last day of multi-day event
      return time.isBefore(endTime);
    } else {
      // Middle day of multi-day event
      return true;
    }
  }

  /**
   * Checks if two events are duplicates.
   */
  private boolean isDuplicateEvent(Event existing, Event newEvent) {
    return existing.getSubject().equals(newEvent.getSubject()) &&
        existing.getStartDate().equals(newEvent.getStartDate()) &&
        Objects.equals(existing.getStartTime(), newEvent.getStartTime());
  }

  /**
   * Checks if an event has conflicts with existing events.
   */
  private boolean hasConflict(Event newEvent) {
    // Check conflicts with single events
    for (Event existing : events) {
      if (existing.conflictsWith(newEvent)) {
        return true;
      }
    }

    // Check conflicts with recurring event instances
    for (RecurringEvent recurring : recurringEvents) {
      for (Event instance : recurring.getInstances()) {
        if (instance.conflictsWith(newEvent)) {
          return true;
        }
      }
    }

    return false;
  }
}