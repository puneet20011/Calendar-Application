package calendar.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Updates event's properties and maintains state of the event.
 * package-private utility class for model.
 */
class EventUpdater {

  private final SystemValidator validator;

  EventUpdater(SystemValidator validator) {
    this.validator = validator;
  }

  /**
   * Creates a new event with updated property.
   *
   * @param event the original event
   * @param property the property to update
   * @param newValue the new value
   * @param preserveSeries whether to keep series association
   * @return new event with updated property
   */
  Event updateProperty(Event event, String property, String newValue, boolean preserveSeries) {
    Event.Builder builder = event.toBuilder();

    switch (property.toLowerCase()) {
      case "subject":
        builder.subject(newValue);
        break;

      case "start":
        updateStartTime(event, newValue, builder, preserveSeries);
        break;

      case "end":
        updateEndTime(event, newValue, builder);
        break;

      case "description":
        builder.description(newValue);
        break;

      case "location":
        builder.location(newValue);
        break;

      case "status":
        builder.status(newValue);
        break;

      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }

    return builder.build();
  }

  /**
   * Updates start time with appropriate logic for series vs standalone events.
   */
  private void updateStartTime(Event event, String newValue, Event.Builder builder,
                               boolean preserveSeries) {
    LocalDateTime parsedStart = parseDateTime(newValue);
    LocalDateTime newStart;
    LocalDateTime newEnd;

    if (event.getSeries() != null) {
      LocalTime newStartTime = parsedStart.toLocalTime();
      newStart = LocalDateTime.of(event.getStart().toLocalDate(), newStartTime);
    } else {
      newStart = parsedStart;
    }

    if (newStart.isBefore(event.getEnd())) {
      newEnd = event.getEnd();
    } else {
      Duration duration = Duration.between(event.getStart(), event.getEnd());
      newEnd = newStart.plus(duration);
    }

    builder.start(newStart).end(newEnd);

    if (!preserveSeries) {
      builder.series(null);
    }
  }

  /**
   * Updates end time with appropriate logic for series vs standalone events.
   */
  private void updateEndTime(Event event, String newValue, Event.Builder builder) {
    LocalDateTime parsedEnd = parseDateTime(newValue);
    LocalDateTime newEnd;

    if (event.getSeries() != null) {
      LocalTime newEndTime = parsedEnd.toLocalTime();
      newEnd = LocalDateTime.of(event.getStart().toLocalDate(), newEndTime);
    } else {
      newEnd = parsedEnd;
    }
    if (newEnd.isBefore(event.getStart()) || newEnd.equals(event.getStart())) {
      throw new IllegalArgumentException(
          "New end time must be after the start time (" + event.getStart() + ")");
    }

    builder.end(newEnd);
  }

  private LocalDateTime parseDateTime(String value) {
    return LocalDateTime.parse(value, CalConstrains.DATE_TIME_FORMATTER);
  }
}
