package calendar.controller;

import calendar.model.CalendarSystemModel;
import calendar.view.IcalendarView;
import java.util.Scanner;

/**
 * Calendar's Controller that manages the application flow and handles user input.
 * Works with any input source through Readable abstraction.
 */
public class CalendarController implements IcalendarController {

  private final CalendarSystemModel model;
  private final IcalendarView view;
  private final CommandExecutor executor;
  private final Scanner scanner;

  /**
   * Creates a Calendar's controller with a custom input source.
   * Accepts any Readable Object, can read from file as well as terminal.
   *
   * @param model the calendar system model (unified interface)
   * @param view  the calendar view
   * @param input the readable input source (InputStreamReader, StringReader, FileReader, etc.)
   */
  public CalendarController(CalendarSystemModel model, IcalendarView view, Readable input) {
    this.model = model;
    this.view = view;
    this.executor = new CommandExecutor(model, view);
    this.scanner = new Scanner(input);
  }

  @Override
  public void run() {
    view.displayMessage("Calendar Application Started");
    view.displayMessage("Enter commands (type 'exit' to quit):");
    view.displayPrompt();

    while (true) {

      if (!scanner.hasNextLine()) {
        view.displayError("File should have Exit command at the end");
        break;
      }

      String line = scanner.nextLine().trim();

      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }

      if (line.equalsIgnoreCase("exit")) {
        view.displayMessage("Goodbye!");
        break;
      }

      processCommand(line);
      view.displayPrompt();
    }

    scanner.close();
  }

  /**
   * Processes a single command line.
   */
  private void processCommand(String commandLine) {
    try {
      boolean success = executor.parseAndExecute(commandLine);

      if (!success) {
        view.displayError("Invalid command: " + commandLine);
      }

    } catch (Exception e) {
      view.displayError("Error: " + e.getMessage());
    }
  }
}