package calendar.view.dialogs;

import calendar.controller.CalendarFeatures;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for copying events between calendars.
 */
public class CopyEventsDialog {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Shows a menu for selecting which type of copy operation to perform.
   *
   * @param parent the parent frame
   * @param features the calendar features controller
   * @param selectedDate the currently selected date
   */
  public static void showMenu(JFrame parent, CalendarFeatures features, LocalDate selectedDate) {
    String[] options = {
        "Copy single event",
        "Copy all events on selected day",
        "Copy events in date range"
    };

    int choice = JOptionPane.showOptionDialog(parent,
        "What would you like to copy?",
        "Copy Events",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (choice == 0) {
      showCopySingleEvent(parent, features, selectedDate);
    } else if (choice == 1) {
      showCopyDayEvents(parent, features, selectedDate);
    } else if (choice == 2) {
      showCopyRange(parent, features, selectedDate);
    }
  }

  private static void showCopySingleEvent(JFrame parent, CalendarFeatures features,
                                          LocalDate selectedDate) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

    JTextField subjectField = new JTextField();
    JTextField sourceTimeField = new JTextField("09:00");
    JComboBox<String> targetCalBox = new JComboBox<>(features.getAvailableCalendars());
    JTextField targetDateField = new JTextField(selectedDate.format(DATE_FMT));
    JTextField targetTimeField = new JTextField("09:00");

    panel.add(new JLabel("Event Subject:"));
    panel.add(subjectField);
    panel.add(new JLabel("Source Start Time:"));
    panel.add(sourceTimeField);
    panel.add(new JLabel("Target Calendar:"));
    panel.add(targetCalBox);
    panel.add(new JLabel("Target Date:"));
    panel.add(targetDateField);
    panel.add(new JLabel("Target Start Time:"));
    panel.add(targetTimeField);

    if (JOptionPane.showConfirmDialog(parent, panel, "Copy Single Event",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      try {
        LocalTime sourceTime = LocalTime.parse(sourceTimeField.getText().trim(), TIME_FMT);
        LocalDateTime sourceStart = LocalDateTime.of(selectedDate, sourceTime);

        LocalDate targetDate = LocalDate.parse(targetDateField.getText().trim(), DATE_FMT);
        LocalTime targetTime = LocalTime.parse(targetTimeField.getText().trim(), TIME_FMT);
        LocalDateTime targetStart = LocalDateTime.of(targetDate, targetTime);

        features.copySingleEvent(
            subjectField.getText().trim(),
            sourceStart,
            (String) targetCalBox.getSelectedItem(),
            targetStart
        );
      } catch (Exception e) {
        JOptionPane.showMessageDialog(parent, "Invalid input: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static void showCopyDayEvents(JFrame parent, CalendarFeatures features,
                                        LocalDate selectedDate) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

    JTextField sourceDateField = new JTextField(selectedDate.format(DATE_FMT));
    JComboBox<String> targetCalBox = new JComboBox<>(features.getAvailableCalendars());
    JTextField targetDateField = new JTextField(selectedDate.format(DATE_FMT));

    panel.add(new JLabel("Source Date:"));
    panel.add(sourceDateField);
    panel.add(new JLabel("Target Calendar:"));
    panel.add(targetCalBox);
    panel.add(new JLabel("Target Date:"));
    panel.add(targetDateField);

    if (JOptionPane.showConfirmDialog(parent, panel, "Copy Events on Date",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      try {
        LocalDate sourceDate = LocalDate.parse(sourceDateField.getText().trim(), DATE_FMT);
        LocalDate targetDate = LocalDate.parse(targetDateField.getText().trim(), DATE_FMT);

        features.copyEventsOnDate(
            sourceDate,
            (String) targetCalBox.getSelectedItem(),
            targetDate
        );
      } catch (Exception e) {
        JOptionPane.showMessageDialog(parent, "Invalid input: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static void showCopyRange(JFrame parent, CalendarFeatures features,
                                    LocalDate selectedDate) {
    JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

    JTextField startDateField = new JTextField(selectedDate.format(DATE_FMT));
    JTextField endDateField = new JTextField(selectedDate.plusDays(7).format(DATE_FMT));
    JComboBox<String> targetCalBox = new JComboBox<>(features.getAvailableCalendars());
    JTextField targetStartField = new JTextField(selectedDate.format(DATE_FMT));

    panel.add(new JLabel("Start Date:"));
    panel.add(startDateField);
    panel.add(new JLabel("End Date (inclusive):"));
    panel.add(endDateField);
    panel.add(new JLabel("Target Calendar:"));
    panel.add(targetCalBox);
    panel.add(new JLabel("Target Start Date:"));
    panel.add(targetStartField);

    if (JOptionPane.showConfirmDialog(parent, panel, "Copy Events in Range",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      try {
        LocalDate startDate = LocalDate.parse(startDateField.getText().trim(), DATE_FMT);
        LocalDate endDate = LocalDate.parse(endDateField.getText().trim(), DATE_FMT);
        LocalDate targetStart = LocalDate.parse(targetStartField.getText().trim(), DATE_FMT);

        features.copyEventsInRange(
            startDate,
            endDate,
            (String) targetCalBox.getSelectedItem(),
            targetStart
        );
      } catch (Exception e) {
        JOptionPane.showMessageDialog(parent, "Invalid input: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}