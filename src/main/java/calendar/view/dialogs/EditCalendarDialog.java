package calendar.view.dialogs;

import calendar.controller.CalendarFeatures;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Dialog for editing calendar properties.
 */
public class EditCalendarDialog {

  /**
   * Shows the edit calendar dialog with options to edit name or timezone.
   *
   * @param parent the parent frame
   * @param features the calendar features controller
   * @param currentCalendar the name of the calendar to edit
   */
  public static void show(JFrame parent, CalendarFeatures features, String currentCalendar) {
    if (currentCalendar == null) {
      JOptionPane.showMessageDialog(parent, "No calendar selected",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String[] options = {"Edit Name", "Edit Timezone"};
    int choice = JOptionPane.showOptionDialog(parent,
        "What would you like to edit?",
        "Edit Calendar: " + currentCalendar,
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (choice == 0) {
      editName(parent, features, currentCalendar);
    } else if (choice == 1) {
      editTimezone(parent, features, currentCalendar);
    }
  }

  private static void editName(JFrame parent, CalendarFeatures features, String currentName) {
    String newName = JOptionPane.showInputDialog(parent,
        "Enter new calendar name:", currentName);

    if (newName != null && !newName.trim().isEmpty()) {
      features.editCalendarProperty(currentName, "name", newName.trim());
    }
  }

  private static void editTimezone(JFrame parent, CalendarFeatures features, String calendarName) {
    String newTz = JOptionPane.showInputDialog(parent,
        "Enter new timezone (e.g., America/Los_Angeles):",
        "America/New_York");

    if (newTz != null && !newTz.trim().isEmpty()) {
      features.editCalendarProperty(calendarName, "timezone", newTz.trim());
    }
  }
}