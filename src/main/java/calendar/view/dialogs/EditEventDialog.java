package calendar.view.dialogs;

import calendar.controller.CalendarFeatures;
import calendar.model.Ievent;
import java.time.format.DateTimeFormatter;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Dialog for editing event properties.
 */
public class EditEventDialog {
  /**
   * Shows the edit options.
   */
  public static void show(JFrame parent, CalendarFeatures features, Ievent event) {
    String[] options = {
        "Edit this event only",
        "Edit this and future events in series",
        "Edit entire series (all events)"
    };

    int editChoice = JOptionPane.showOptionDialog(parent,
        "How would you like to edit '" + event.getSubject() + "'?",
        "Edit Event",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (editChoice < 0) {
      return;
    }

    String[] properties = {"subject", "start", "end", "description", "location", "status"};
    String property = (String) JOptionPane.showInputDialog(parent,
        "Select property to edit:",
        "Edit Property",
        JOptionPane.QUESTION_MESSAGE,
        null,
        properties,
        properties[0]);

    if (property == null) {
      return;
    }

    String newValue = getNewValueForProperty(parent, event, property);
    if (newValue == null || newValue.trim().isEmpty()) {
      return;
    }

    if (editChoice == 0) {
      features.editSingleEvent(event.getSubject(), event.getStart(), property, newValue);
    } else if (editChoice == 1) {
      features.editMultipleEvents(event.getSubject(), event.getStart(), property, newValue);
    } else if (editChoice == 2) {
      features.editEntireSeries(event.getSubject(), event.getStart(), property, newValue);
    }
  }

  private static String getNewValueForProperty(JFrame parent, Ievent event, String property) {
    if (property.equals("status")) {
      return (String) JOptionPane.showInputDialog(parent,
          "Select new status:",
          "Edit Status",
          JOptionPane.QUESTION_MESSAGE,
          null,
          new String[] {"public", "private"},
          event.getStatus());

    } else if (property.equals("start") || property.equals("end")) {
      String current = property.equals("start")
          ? event.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) :
          event.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
      return JOptionPane.showInputDialog(parent,
          "Enter new " + property + " (yyyy-MM-dd'T'HH:mm):",
          current);

    } else {
      String current = "";
      if (property.equals("subject")) {
        current = event.getSubject();
      } else if (property.equals("description")) {
        current = event.getDescription() != null ? event.getDescription() : "";
      } else if (property.equals("location")) {
        current = event.getLocation() != null ? event.getLocation() : "";
      }
      return JOptionPane.showInputDialog(parent, "Enter new " + property + ":", current);
    }
  }
}