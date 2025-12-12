package model.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Represents a recurring event series.
 */
public class RecurringEvent {
  private final String subject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final EventVisibility visibility;
  private final String description;
  private final String location;
  private final RecurrencePattern pattern;
  private final List<RecurringEventInstance> instances;
  private final Map<LocalDate, RecurringEventInstance> instanceMap;

  /**
   * Creates a recurring event.
   */
  public RecurringEvent(String subject, LocalTime startTime, LocalTime endTime,
      EventVisibility visibility, String description,
      String location, RecurrencePattern pattern,
      LocalDate startDate) {
    this.subject = Objects.requireNonNull(subject);
    this.startTime = Objects.requireNonNull(startTime);
    this.endTime = endTime;
    this.visibility = Objects.requireNonNull(visibility);
    this.description = description;
    this.location = location;
    this.pattern = Objects.requireNonNull(pattern);
    this.instances = new ArrayList<>();
    this.instanceMap = new HashMap<>();

    generateInstances(startDate);
  }

  private void generateInstances(LocalDate startDate) {
    LocalDate current = startDate;
    int count = 0;
    int targetOccurrences = pattern.isOccurrenceBased() ? pattern.getOccurrences() : Integer.MAX_VALUE;
    LocalDate endDate = pattern.getEndDate();

    while (count < targetOccurrences && (endDate == null || !current.isAfter(endDate))) {
      DayOfWeek dayOfWeek = DayOfWeek.values()[current.getDayOfWeek().getValue() % 7];
      if (pattern.getDaysOfWeek().contains(dayOfWeek)) {
        RecurringEventInstance instance = new RecurringEventInstance(
            this, current, false
        );
        instances.add(instance);
        instanceMap.put(current, instance);
        count++;
      }
      current = current.plusDays(1);

      // Safety check to prevent infinite loops
      if (current.isAfter(startDate.plusYears(5))) {
        break;
      }
    }
  }

  /**
   * Gets all instances of the recurring event.
   */
  public List<Event> getInstances() {
    return new ArrayList<>(instances);
  }

  /**
   * Gets an instance on a specific date.
   */
  public Optional<RecurringEventInstance> getInstance(LocalDate date) {
    return Optional.ofNullable(instanceMap.get(date));
  }

  /**
   * Modifies a single instance.
   */
  public void modifyInstance(LocalDate date, EventBuilder updates) {
    RecurringEventInstance instance = instanceMap.get(date);
    if (instance != null) {
      instance.modify(updates);
    }
  }

  /**
   * Modifies all instances from a specific date.
   */
  public void modifyFrom(LocalDate fromDate, EventBuilder updates) {
    for (RecurringEventInstance instance : instances) {
      if (!instance.getStartDate().isBefore(fromDate)) {
        instance.modify(updates);
      }
    }
  }

  /**
   * Modifies all instances in the series.
   */
  public void modifyAll(EventBuilder updates) {
    for (RecurringEventInstance instance : instances) {
      instance.modify(updates);
    }
  }

  // Getters
  public String getSubject() { return subject; }
  public LocalTime getStartTime() { return startTime; }
  public LocalTime getEndTime() { return endTime; }
  public EventVisibility getVisibility() { return visibility; }
  public String getDescription() { return description; }
  public String getLocation() { return location; }
}