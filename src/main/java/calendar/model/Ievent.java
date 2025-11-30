package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a calendar event.
 * Provides methods to get the Event Data.
 */
public interface Ievent {

  /**
   * Gets the event subject.
   */
  String getSubject();

  /**
   * Gets the event start date and time.
   */
  LocalDateTime getStart();

  /**
   * Gets the event end date and time.
   */
  LocalDateTime getEnd();

  /**
   * Gets the event description.
   */
  String getDescription();

  /**
   * Gets the event location.
   */
  String getLocation();

  /**
   * Gets the event status (public/private).
   */
  String getStatus();
}