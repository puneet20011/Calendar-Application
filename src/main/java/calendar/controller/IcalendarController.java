package calendar.controller;

/**
 * Represents calendar controller.
 * The controller manages the application flow by reading commands from an input source
 * and coordinating between the model and view.
 */
public interface IcalendarController {

  /**
   * Starts the controller's main loop.
   * Reads commands from the input source until exit or EOF is reached.
   * Works for both interactive terminal mode and headless file mode.
   */
  void run();
}