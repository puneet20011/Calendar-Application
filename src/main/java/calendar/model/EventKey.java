package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents event key for the events.
 * Provides equals method to  check if both keys are same.
 * package-private utility class for model.
 */
class EventKey {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Executes parsed commands by delegating to the model and view.
   * Pattern matching is handled by CommandPatterns class.
   */
  EventKey(String subject, LocalDateTime start, LocalDateTime end) {
    this.subject = subject;
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventKey eventKey = (EventKey) o;
    return Objects.equals(subject, eventKey.subject)
        && Objects.equals(start, eventKey.start)
        && Objects.equals(end, eventKey.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }
}
