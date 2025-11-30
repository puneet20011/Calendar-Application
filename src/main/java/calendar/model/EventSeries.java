package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a series of recurring events.
 * Use the Builder pattern to create instances.
 * Supports adding and removing events from event series list.
 * package-private utility class for model.
 */
class EventSeries {
  private final String baseSubject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final Set<DayOfWeek> repeatDays;
  private final List<Event> occurrences;


  EventSeries(String subject, LocalTime start, LocalTime end, Set<DayOfWeek> days) {
    this.baseSubject = subject;
    this.startTime = start;
    this.endTime = end;
    this.repeatDays = days;
    this.occurrences = new ArrayList<>();
  }


  /**
   * Creates a Builder with required fields specified.
   *
   * @param subject    the series subject (required)
   * @param startTime  the start time for all occurrences (required)
   * @param endTime    the end time for all occurrences (required)
   * @param repeatDays the days of week to repeat on (required)
   * @return a Builder instance
   */
  static Builder builder(String subject, LocalTime startTime, LocalTime endTime,
                         Set<DayOfWeek> repeatDays) {
    return new Builder(subject, startTime, endTime, repeatDays);
  }

  /**
   * Creates an empty Builder.
   *
   * @return a Builder instance
   */
  static Builder builder() {
    return new Builder();
  }


  static class Builder {
    private String baseSubject;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<DayOfWeek> repeatDays;


    private Builder() {
    }


    private Builder(String subject, LocalTime startTime, LocalTime endTime,
                    Set<DayOfWeek> repeatDays) {
      this.baseSubject = subject;
      this.startTime = startTime;
      this.endTime = endTime;
      this.repeatDays = repeatDays;
    }

    public Builder subject(String subject) {
      this.baseSubject = subject;
      return this;
    }

    public Builder startTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder endTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder repeatDays(Set<DayOfWeek> repeatDays) {
      this.repeatDays = repeatDays;
      return this;
    }

    public EventSeries build() {
      if (baseSubject == null || baseSubject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject is required");
      }
      if (startTime == null) {
        throw new IllegalArgumentException("Start time is required");
      }
      if (endTime == null) {
        throw new IllegalArgumentException("End time is required");
      }
      if (repeatDays == null || repeatDays.isEmpty()) {
        throw new IllegalArgumentException("Repeat days are required");
      }

      return new EventSeries(baseSubject, startTime, endTime, repeatDays);
    }
  }


  void addOccurrence(Event event) {
    occurrences.add(event);
  }

  void removeOccurrence(Event event) {
    occurrences.remove(event);
  }

  List<Event> getAllOccurrences() {
    return new ArrayList<>(occurrences);
  }

  List<Event> getOccurrencesFromDate(LocalDate fromDate) {
    List<Event> result = new ArrayList<>();
    for (Event event : occurrences) {
      if (!event.getStart().toLocalDate().isBefore(fromDate)) {
        result.add(event);
      }
    }
    return result;
  }

  Set<DayOfWeek> getRepeatDays() {
    return repeatDays;
  }
}