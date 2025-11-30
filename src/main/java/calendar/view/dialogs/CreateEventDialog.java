package calendar.view.dialogs;

import calendar.controller.CalendarFeatures;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for creating single or event serie.
 */
public class CreateEventDialog {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Shows the create event dialog with tabs for single and recurring events.
   */
  public static void show(JFrame parent, CalendarFeatures features, LocalDate selectedDate) {
    JDialog dialog = new JDialog(parent, "Create Event on "
        + selectedDate.format(DATE_FMT), true);
    dialog.setLayout(new BorderLayout(10, 10));

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Single/All-Day Event", createSingleEventPanel(dialog, features, selectedDate));
    tabs.addTab("Recurring Event", createEventSeriesPanel(dialog, features, selectedDate));

    dialog.add(tabs, BorderLayout.CENTER);
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }

  private static JPanel createSingleEventPanel(JDialog dialog, CalendarFeatures features,
                                               LocalDate selectedDate) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    final JTextField subjectField = new JTextField();
    JCheckBox allDayCheckbox = new JCheckBox("All-Day Event (8am - 5pm)");
    JTextField startField = new JTextField("09:00");
    JTextField endField = new JTextField("10:00");
    final JTextField descField = new JTextField();
    final JTextField locField = new JTextField();
    final JComboBox<String> statusBox = new JComboBox<>(new String[] {"public", "private"});

    setupAllDayCheckbox(allDayCheckbox, startField, endField);
    addSingleEventFieldsToPanel(panel, subjectField, allDayCheckbox, startField,
        endField, descField, locField, statusBox);

    JButton createBtn = new JButton("Create Event");
    createBtn.addActionListener(e -> handleSingleEventCreation(dialog, features, selectedDate,
        subjectField, startField, endField, descField, locField, statusBox));

    return addButtonToPanel(panel, createBtn);
  }

  private static void setupAllDayCheckbox(JCheckBox allDayCheckbox,
                                          JTextField startField, JTextField endField) {
    allDayCheckbox.addActionListener(e -> {
      boolean isAllDay = allDayCheckbox.isSelected();
      startField.setEnabled(!isAllDay);
      endField.setEnabled(!isAllDay);
      if (isAllDay) {
        startField.setText("08:00");
        endField.setText("17:00");
      }
    });
  }

  private static void addSingleEventFieldsToPanel(JPanel panel, JTextField subjectField,
                                                  JCheckBox allDayCheckbox, JTextField startField,
                                                  JTextField endField, JTextField descField,
                                                  JTextField locField,
                                                  JComboBox<String> statusBox) {
    panel.add(new JLabel("Subject:*"));
    panel.add(subjectField);
    panel.add(new JLabel(""));
    panel.add(allDayCheckbox);
    panel.add(new JLabel("Start Time (HH:mm):*"));
    panel.add(startField);
    panel.add(new JLabel("End Time (HH:mm):*"));
    panel.add(endField);
    panel.add(new JLabel("Description:"));
    panel.add(descField);
    panel.add(new JLabel("Location:"));
    panel.add(locField);
    panel.add(new JLabel("Status:"));
    panel.add(statusBox);
  }

  private static void handleSingleEventCreation(JDialog dialog, CalendarFeatures features,
                                                LocalDate selectedDate, JTextField subjectField,
                                                JTextField startField, JTextField endField,
                                                JTextField descField, JTextField locField,
                                                JComboBox<String> statusBox) {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Subject is required",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      LocalTime start = LocalTime.parse(startField.getText().trim(), TIME_FMT);
      LocalTime end = LocalTime.parse(endField.getText().trim(), TIME_FMT);

      features.createSingleEvent(
          subject,
          LocalDateTime.of(selectedDate, start),
          LocalDateTime.of(selectedDate, end),
          descField.getText().trim(),
          locField.getText().trim(),
          (String) statusBox.getSelectedItem()
      );

      dialog.dispose();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private static JPanel createEventSeriesPanel(JDialog dialog, CalendarFeatures features,
                                                  LocalDate selectedDate) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    final JTextField subjectField = new JTextField();
    final JTextField startField = new JTextField("09:00");
    final JTextField endField = new JTextField("10:00");
    final JTextField descField = new JTextField();
    final JTextField locField = new JTextField();

    JCheckBox[] dayBoxes = new JCheckBox[7];
    JPanel daysPanel = createWeekdaySelectionPanel(dayBoxes);

    JRadioButton countRadio = new JRadioButton("Count:", true);
    JRadioButton untilRadio = new JRadioButton("Until:");
    ButtonGroup group = new ButtonGroup();
    group.add(countRadio);
    group.add(untilRadio);

    JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    JTextField untilField = new JTextField(LocalDate.now().plusMonths(1).format(DATE_FMT));

    addRecurringEventFieldsToPanel(panel, subjectField, startField, endField,
        descField, locField, daysPanel, countRadio,
        countSpinner, untilRadio, untilField);

    JButton createBtn = new JButton("Create Recurring Event");
    createBtn.addActionListener(e -> handleRecurringEventCreation(dialog, features, selectedDate,
        subjectField, startField, endField, descField, locField, dayBoxes,
        countRadio, countSpinner, untilRadio, untilField));

    return addButtonToPanel(panel, createBtn);
  }

  private static JPanel createWeekdaySelectionPanel(JCheckBox[] dayBoxes) {
    JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (int i = 0; i < 7; i++) {
      dayBoxes[i] = new JCheckBox(dayLabels[i]);
      daysPanel.add(dayBoxes[i]);
    }
    return daysPanel;
  }

  private static void addRecurringEventFieldsToPanel(JPanel panel, JTextField subjectField,
                                                     JTextField startField, JTextField endField,
                                                     JTextField descField, JTextField locField,
                                                     JPanel daysPanel, JRadioButton countRadio,
                                                     JSpinner countSpinner, JRadioButton untilRadio,
                                                     JTextField untilField) {
    panel.add(new JLabel("Subject:*"));
    panel.add(subjectField);
    panel.add(new JLabel("Start Time:*"));
    panel.add(startField);
    panel.add(new JLabel("End Time:*"));
    panel.add(endField);
    panel.add(new JLabel("Description:"));
    panel.add(descField);
    panel.add(new JLabel("Location:"));
    panel.add(locField);
    panel.add(new JLabel("Repeat On:*"));
    panel.add(daysPanel);
    panel.add(countRadio);
    panel.add(countSpinner);
    panel.add(untilRadio);
    panel.add(untilField);
  }

  private static void handleRecurringEventCreation(JDialog dialog, CalendarFeatures features,
                                                   LocalDate selectedDate, JTextField subjectField,
                                                   JTextField startField, JTextField endField,
                                                   JTextField descField, JTextField locField,
                                                   JCheckBox[] dayBoxes, JRadioButton countRadio,
                                                   JSpinner countSpinner, JRadioButton untilRadio,
                                                   JTextField untilField) {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Subject is required",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String weekdays = extractSelectedWeekdays(dayBoxes);
      if (weekdays.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Select at least one weekday",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      LocalTime start = LocalTime.parse(startField.getText().trim(), TIME_FMT);
      LocalTime end = LocalTime.parse(endField.getText().trim(), TIME_FMT);

      int occurrences = countRadio.isSelected()
          ? (Integer) countSpinner.getValue() : -1;
      LocalDate until = untilRadio.isSelected()
          ? LocalDate.parse(untilField.getText().trim(), DATE_FMT) : null;

      features.createRecurringEvent(
          subject,
          LocalDateTime.of(selectedDate, start),
          LocalDateTime.of(selectedDate, end),
          weekdays,
          occurrences,
          until,
          descField.getText().trim(),
          locField.getText().trim(),
          "public"
      );

      dialog.dispose();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private static String extractSelectedWeekdays(JCheckBox[] dayBoxes) {
    final String[] dayNames = {"M", "T", "W", "R", "F", "S", "U"};
    StringBuilder weekdays = new StringBuilder();
    for (int i = 0; i < 7; i++) {
      if (dayBoxes[i].isSelected()) {
        weekdays.append(dayNames[i]);
      }
    }
    return weekdays.toString();
  }

  private static JPanel addButtonToPanel(JPanel contentPanel, JButton button) {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(button);

    JPanel main = new JPanel(new BorderLayout());
    main.add(contentPanel, BorderLayout.CENTER);
    main.add(buttonPanel, BorderLayout.SOUTH);

    return main;
  }
}