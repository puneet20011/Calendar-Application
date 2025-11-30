package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a calendar event with immutable properties.
 * Use the Builder pattern to create instances.
 * Not a data class overrides equal and hashcode.
 * package-private utility class for model.
 */
public class Event implements Ievent {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final EventSeries series;
  private final String description;
  private final String location;
  private final String status;

  Event(String subject, LocalDateTime start, LocalDateTime end, EventSeries series,
        String description, String location, String status) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.series = series;
    this.description = description;
    this.location = location;
    this.status = status;
  }


  /**
   * Creates a Builder with required fields specified.
   * Recommended approach for creating new events.
   *
   * @param subject the event subject (required)
   * @param start   the start date/time (required)
   * @param end     the end date/time (required)
   * @return a Builder instance
   */
  static Builder builder(String subject, LocalDateTime start, LocalDateTime end) {
    return new Builder(subject, start, end);
  }

  /**
   * Creates an empty Builder.
   * Use for flexibility when building from existing data.
   *
   * @return a Builder instance
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a Builder initialized with this event's values.
   * Useful for creating modified copies of events.
   *
   * @return a Builder instance with current values
   */
  Builder toBuilder() {
    return new Builder()
        .subject(this.subject)
        .start(this.start)
        .end(this.end)
        .series(this.series)
        .description(this.description)
        .location(this.location)
        .status(this.status);
  }


  static class Builder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventSeries series;
    private String description;
    private String location;
    private String status = "public";

    private Builder() {
    }


    private Builder(String subject, LocalDateTime start, LocalDateTime end) {
      this.subject = subject;
      this.start = start;
      this.end = end;
    }

    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder start(LocalDateTime start) {
      this.start = start;
      return this;
    }

    public Builder end(LocalDateTime end) {
      this.end = end;
      return this;
    }

    public Builder series(EventSeries series) {
      this.series = series;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Event build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject is required and cannot be empty");
      }
      if (start == null) {
        throw new IllegalArgumentException("Start time is required");
      }
      if (end == null) {
        throw new IllegalArgumentException("End time is required");
      }

      return new Event(subject, start, end, series, description, location, status);
    }
  }


  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  EventSeries getSeries() {
    return series;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(subject, event.subject)
        && Objects.equals(start, event.start)
        && Objects.equals(end, event.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }
}