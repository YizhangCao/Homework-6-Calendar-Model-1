package controller;

import model.calendar.CalendarImpl;
import model.calendar.CalendarManager;
import model.event.Event;
import model.event.EventBuilder;
import view.CreateEventView;
import view.EventDetailView;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Main controller for the calendar application.
 * Handles initialization, calendar management, and view creation.
 * 
 * Note: The code connecting views to the model is NOT AI-generated
 * per assignment requirements.
 */
public class CalendarController {

  private final CalendarManager manager;
  private CalendarImpl currentCalendar;

  /**
   * Creates a new CalendarController with the default calendar manager.
   */
  public CalendarController() {
    this.manager = new CalendarManager();
  }

  /**
   * Creates a new CalendarController with a custom calendar manager.
   *
   * @param manager the calendar manager to use
   */
  public CalendarController(CalendarManager manager) {
    this.manager = manager;
  }

  /**
   * Initializes the controller by restoring saved calendars.
   * If no calendars exist, creates a default calendar with sample events.
   */
  public void initialize() {
    try {
      manager.restoreAll();
      System.out.println("Calendars restored successfully.");
    } catch (IOException e) {
      System.out.println("No saved calendars found or error loading: " + e.getMessage());
    }

    // If no calendars exist, create a default one
    if (manager.getAllCalendars().isEmpty()) {
      currentCalendar = manager.createCalendar("My Calendar");
      createSampleEvents();
      System.out.println("Created default calendar with sample events.");
    } else {
      // Select the first calendar arbitrarily
      currentCalendar = manager.getAllCalendars().get(0);
      System.out.println("Selected calendar: " + currentCalendar.getTitle());
    }
  }

  /**
   * Creates sample events for demonstration purposes.
   */
  private void createSampleEvents() {
    // Create a few sample events
    Event meeting = EventBuilder.create()
        .withSubject("Team Meeting")
        .withStartDate(LocalDate.now())
        .withStartTime(LocalTime.of(10, 0))
        .withEndTime(LocalTime.of(11, 0))
        .withLocation("Conference Room A")
        .withDescription("Weekly team sync")
        .build();
    currentCalendar.addEvent(meeting);

    Event lunch = EventBuilder.create()
        .withSubject("Lunch with Client")
        .withStartDate(LocalDate.now().plusDays(1))
        .withStartTime(LocalTime.of(12, 30))
        .withEndTime(LocalTime.of(13, 30))
        .withLocation("Downtown Cafe")
        .build();
    currentCalendar.addEvent(lunch);

    Event allDay = EventBuilder.create()
        .withSubject("Company Holiday")
        .withStartDate(LocalDate.now().plusDays(7))
        .build();
    currentCalendar.addEvent(allDay);
  }

  /**
   * Gets the current calendar.
   *
   * @return the current calendar
   */
  public CalendarImpl getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Gets the calendar manager.
   *
   * @return the calendar manager
   */
  public CalendarManager getManager() {
    return manager;
  }

  /**
   * Saves all calendars.
   */
  public void saveAll() {
    try {
      manager.saveAll();
      System.out.println("Calendars saved successfully.");
    } catch (IOException e) {
      System.err.println("Error saving calendars: " + e.getMessage());
    }
  }

  /**
   * Opens the CreateEventView for creating a new event.
   */
  public void openCreateEventView() {
    CreateEventView view = new CreateEventView(currentCalendar);
    view.setVisible(true);
  }

  /**
   * Opens the EventDetailView for viewing/editing an event.
   *
   * @param event the event to display
   */
  public void openEventDetailView(Event event) {
    EventDetailView view = new EventDetailView(currentCalendar, event);
    view.setVisible(true);
  }

  /**
   * Main entry point for the calendar application.
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    // Set look and feel to system default
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // Use default look and feel
    }

    // Run on EDT
    SwingUtilities.invokeLater(() -> {
      CalendarController controller = new CalendarController();

      // Step 1: Restore calendars from previous runs
      controller.initialize();

      CalendarImpl calendar = controller.getCurrentCalendar();

      // Step 2: Calendar is already selected in initialize()
      System.out.println("Using calendar: " + calendar.getTitle());

      // Step 3: Create and display the two views
      // Open CreateEventView
      controller.openCreateEventView();

      // Open EventDetailView with an arbitrary event
      List<Event> events = calendar.getAllEvents();
      if (!events.isEmpty()) {
        // Choose first event arbitrarily
        Event firstEvent = events.get(0);
        controller.openEventDetailView(firstEvent);
        System.out.println("Displaying event: " + firstEvent.getSubject());
      } else {
        System.out.println("No events to display in EventDetailView.");
      }

      // Add shutdown hook to save calendars on exit
      Runtime.getRuntime().addShutdownHook(new Thread(controller::saveAll));
    });
  }
}
