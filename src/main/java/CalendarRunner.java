import calendar.controller.CalendarController;
import calendar.controller.CalendarGuiController;
import calendar.controller.IcalendarController;
import calendar.model.CalendarSystem;
import calendar.model.CalendarSystemModel;
import calendar.view.CalendarSwingView;
import calendar.view.CalendarView;
import calendar.view.IcalendarGuiView;
import calendar.view.IcalendarView;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.SwingUtilities;

/**
 * Main runner class for the Calendar application.
 * Supports GUI mode, interactive text mode, and headless file mode.
 */
public class CalendarRunner {

  /**
   * Main entry point for the Calendar application.
   *
   * @param args command line arguments for specifying mode and options
   */
  public static void main(String[] args) {
    System.setOut(new java.io.PrintStream(System.out, true));
    System.setErr(new java.io.PrintStream(System.err, true));

    CalendarSystemModel model = new CalendarSystem();

    if (args.length == 0) {
      SwingUtilities.invokeLater(() -> {
        IcalendarGuiView view = new CalendarSwingView();
        CalendarGuiController controller = new CalendarGuiController(model, view);
        controller.run();
      });
      return;
    }

    IcalendarView view = new CalendarView();

    if (args.length < 2) {
      printUsage();
      System.exit(1);
    }

    String modeFlag = args[0].toLowerCase();
    String mode = args[1].toLowerCase();

    if (!modeFlag.equals("--mode")) {
      System.err.println("Invalid argument. Expected --mode");
      printUsage();
      System.exit(1);
    }

    if (mode.equals("interactive")) {
      IcalendarController controller = new CalendarController(
          model, view, new InputStreamReader(System.in));
      controller.run();
      return;
    }

    if (mode.equals("headless")) {
      if (args.length < 3) {
        System.err.println("Headless mode requires a file path");
        printUsage();
        System.exit(1);
      }

      String filePath = args[2];
      try (FileReader fileReader = new FileReader(filePath)) {
        IcalendarController controller = new CalendarController(model, view, fileReader);
        controller.run();
      } catch (IOException e) {
        System.err.println("Error reading command file: " + e.getMessage());
        System.exit(1);
      }
      return;
    }

    System.err.println("Invalid mode: " + mode);
    printUsage();
    System.exit(1);
  }

  private static void printUsage() {
    System.err.println("Usage: java -jar CalendarApp.jar                  (GUI mode)");
    System.err.println("   or: java -jar CalendarApp.jar --mode interactive");
    System.err.println("   or: java -jar CalendarApp.jar --mode headless <file>");
  }
}