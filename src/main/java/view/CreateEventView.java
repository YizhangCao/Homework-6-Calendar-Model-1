package view;

import model.calendar.Calendar;
import model.calendar.CalendarImpl;
import model.event.EventBuilder;
import model.event.EventVisibility;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * View for creating a new event in a calendar.
 * AI-generated Swing implementation.
 */
public class CreateEventView extends JFrame {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final CalendarImpl calendar;

  private JTextField subjectField;
  private JTextField startDateField;
  private JTextField startTimeField;
  private JTextField endDateField;
  private JTextField endTimeField;
  private JTextArea descriptionArea;
  private JTextField locationField;
  private JCheckBox allDayCheckBox;
  private JComboBox<EventVisibility> visibilityCombo;

  /**
   * Creates a new CreateEventView for the given calendar.
   *
   * @param calendar the calendar to add events to
   */
  public CreateEventView(CalendarImpl calendar) {
    this.calendar = calendar;
    initializeUI();
  }

  /**
   * Initializes the user interface components.
   */
  private void initializeUI() {
    setTitle("Create New Event - " + calendar.getTitle());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    // Main form panel
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    int row = 0;

    // Subject
    gbc.gridx = 0;
    gbc.gridy = row;
    formPanel.add(new JLabel("Subject:*"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    subjectField = new JTextField(30);
    formPanel.add(subjectField, gbc);
    row++;

    // Start Date
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Start Date:* (yyyy-MM-dd)"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    startDateField = new JTextField(LocalDate.now().format(DATE_FORMAT));
    formPanel.add(startDateField, gbc);
    row++;

    // All Day Checkbox
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("All Day Event:"), gbc);
    gbc.gridx = 1;
    allDayCheckBox = new JCheckBox();
    allDayCheckBox.addActionListener(e -> toggleTimeFields());
    formPanel.add(allDayCheckBox, gbc);
    row++;

    // Start Time
    gbc.gridx = 0;
    gbc.gridy = row;
    formPanel.add(new JLabel("Start Time: (HH:mm)"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    startTimeField = new JTextField("09:00");
    formPanel.add(startTimeField, gbc);
    row++;

    // End Date
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("End Date: (yyyy-MM-dd)"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    endDateField = new JTextField(LocalDate.now().format(DATE_FORMAT));
    formPanel.add(endDateField, gbc);
    row++;

    // End Time
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("End Time: (HH:mm)"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    endTimeField = new JTextField("10:00");
    formPanel.add(endTimeField, gbc);
    row++;

    // Visibility
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    formPanel.add(new JLabel("Visibility:"), gbc);
    gbc.gridx = 1;
    visibilityCombo = new JComboBox<>(EventVisibility.values());
    formPanel.add(visibilityCombo, gbc);
    row++;

    // Location
    gbc.gridx = 0;
    gbc.gridy = row;
    formPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    locationField = new JTextField(30);
    formPanel.add(locationField, gbc);
    row++;

    // Description
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    formPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    descriptionArea = new JTextArea(4, 30);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    formPanel.add(new JScrollPane(descriptionArea), gbc);

    add(formPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    buttonPanel.add(cancelButton);

    JButton createButton = new JButton("Create Event");
    createButton.addActionListener(e -> createEvent());
    buttonPanel.add(createButton);

    add(buttonPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(null);
  }

  /**
   * Toggles the time fields based on the all-day checkbox.
   */
  private void toggleTimeFields() {
    boolean enabled = !allDayCheckBox.isSelected();
    startTimeField.setEnabled(enabled);
    endTimeField.setEnabled(enabled);
  }

  /**
   * Creates the event from the form data.
   */
  private void createEvent() {
    try {
      EventBuilder builder = EventBuilder.create();

      // Required fields
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        showError("Subject is required");
        return;
      }
      builder.withSubject(subject);

      LocalDate startDate;
      try {
        startDate = LocalDate.parse(startDateField.getText().trim(), DATE_FORMAT);
      } catch (DateTimeParseException e) {
        showError("Invalid start date format. Use yyyy-MM-dd");
        return;
      }
      builder.withStartDate(startDate);

      // Optional time fields
      if (!allDayCheckBox.isSelected()) {
        String startTimeText = startTimeField.getText().trim();
        if (!startTimeText.isEmpty()) {
          try {
            builder.withStartTime(LocalTime.parse(startTimeText, TIME_FORMAT));
          } catch (DateTimeParseException e) {
            showError("Invalid start time format. Use HH:mm");
            return;
          }
        }

        String endTimeText = endTimeField.getText().trim();
        if (!endTimeText.isEmpty()) {
          try {
            builder.withEndTime(LocalTime.parse(endTimeText, TIME_FORMAT));
          } catch (DateTimeParseException e) {
            showError("Invalid end time format. Use HH:mm");
            return;
          }
        }
      }

      // End date
      String endDateText = endDateField.getText().trim();
      if (!endDateText.isEmpty()) {
        try {
          builder.withEndDate(LocalDate.parse(endDateText, DATE_FORMAT));
        } catch (DateTimeParseException e) {
          showError("Invalid end date format. Use yyyy-MM-dd");
          return;
        }
      }

      // Visibility
      builder.withVisibility((EventVisibility) visibilityCombo.getSelectedItem());

      // Location
      String location = locationField.getText().trim();
      if (!location.isEmpty()) {
        builder.withLocation(location);
      }

      // Description
      String description = descriptionArea.getText().trim();
      if (!description.isEmpty()) {
        builder.withDescription(description);
      }

      // Add to calendar
      boolean added = calendar.addEvent(builder.build());
      if (added) {
        JOptionPane.showMessageDialog(this,
            "Event created successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        dispose();
      } else {
        showError("Failed to add event. It may conflict with an existing event.");
      }

    } catch (IllegalStateException e) {
      showError("Invalid event configuration: " + e.getMessage());
    }
  }

  /**
   * Shows an error message dialog.
   */
  private void showError(String message) {
    JOptionPane.showMessageDialog(this,
        message,
        "Error",
        JOptionPane.ERROR_MESSAGE);
  }
}
