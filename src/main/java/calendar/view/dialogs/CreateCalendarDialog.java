package calendar.view.dialogs;

import calendar.controller.CalendarFeatures;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for creating a new calendar.
 */
public class CreateCalendarDialog {

  /**
   * Shows the create calendar dialog.
   *
   * @param parent the parent frame
   * @param features the calendar features controller
   */
  public static void show(JFrame parent, CalendarFeatures features) {
    JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

    JTextField nameField = new JTextField();
    JTextField tzField = new JTextField("America/New_York");

    panel.add(new JLabel("Calendar Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    panel.add(tzField);
    panel.add(new JLabel("(e.g., America/New_York)"));
    panel.add(new JLabel(""));

    int result = JOptionPane.showConfirmDialog(parent, panel, "Create New Calendar",
        JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      String name = nameField.getText().trim();
      String timezone = tzField.getText().trim();

      if (name.isEmpty() || timezone.isEmpty()) {
        JOptionPane.showMessageDialog(parent, "Name and timezone are required",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      features.createCalendar(name, timezone);
    }
  }
}