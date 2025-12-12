package view;

import model.calendar.CalendarImpl;
import model.event.Event;
import model.event.EventBuilder;
import model.event.EventVisibility;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * View for displaying and modifying an existing event.
 * AI-generated Swing implementation.
 */
public class EventDetailView extends JFrame {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final CalendarImpl calendar;
  private final Event event;

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
   * Creates a new EventDetailView for the given event.
   *
   * @param calendar the calendar containing the event
   * @param event the event to display and potentially modify
   */
  public EventDetailView(CalendarImpl calendar, Event event) {
    this.calendar = calendar;
    this.event = event;
    initializeUI();
    populateFields();
  }

  /**
   * Initializes the user interface components.
   */
  private void initializeUI() {
    setTitle("Event Details - " + event.getSubject());
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
    startDateField = new JTextField();
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
    startTimeField = new JTextField();
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
    endDateField = new JTextField();
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
    endTimeField = new JTextField();
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

    JButton saveButton = new JButton("Save Changes");
    saveButton.addActionListener(e -> saveChanges());
    buttonPanel.add(saveButton);

    add(buttonPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(null);
  }

  /**
   * Populates the form fields with the event's current data.
   */
  private void populateFields() {
    subjectField.setText(event.getSubject());
    startDateField.setText(event.getStartDate().format(DATE_FORMAT));

    if (event.isAllDay()) {
      allDayCheckBox.setSelected(true);
      startTimeField.setEnabled(false);
      endTimeField.setEnabled(false);
    } else {
      event.getStartTime().ifPresent(time ->
          startTimeField.setText(time.format(TIME_FORMAT)));
      event.getEndTime().ifPresent(time ->
          endTimeField.setText(time.format(TIME_FORMAT)));
    }

    event.getEndDate().ifPresent(date ->
        endDateField.setText(date.format(DATE_FORMAT)));

    visibilityCombo.setSelectedItem(event.getVisibility());

    event.getLocation().ifPresent(locationField::setText);
    event.getDescription().ifPresent(descriptionArea::setText);
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
   * Saves the changes to the event.
   */
  private void saveChanges() {
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

      // Update in calendar
      boolean updated = calendar.updateEvent(event, builder);
      if (updated) {
        JOptionPane.showMessageDialog(this,
            "Event updated successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        dispose();
      } else {
        showError("Failed to update event. The changes may conflict with another event.");
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
