package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for EventKey equality and hashing.
 */
public class EventKeyTest {

  private LocalDateTime start;
  private LocalDateTime end;
  private EventKey key1;

  /**
   * Prepares a baseline event key before each test.
   */
  @Before
  public void setUp() {
    start = LocalDateTime.of(2025, 1, 15, 10, 0);
    end = LocalDateTime.of(2025, 1, 15, 11, 0);
    key1 = new EventKey("Subject", start, end);
  }

  @Test
  public void testEqualsSymmetric() {
    EventKey key2 = new EventKey("Subject", start, end);
    assertTrue(key1.equals(key2));
    assertTrue(key2.equals(key1));
  }

  @Test
  public void testEqualsSameReference() {
    assertTrue(key1.equals(key1));
  }

  @Test
  public void testEqualsNull() {
    assertFalse(key1.equals(null));
  }

  @Test
  public void testEqualsDifferentClass() {
    assertFalse(key1.equals(new Object()));
  }

  @Test
  public void testEqualsDifferentSubject() {
    EventKey key2 = new EventKey("Different Subject", start, end);
    assertFalse(key1.equals(key2));
  }

  @Test
  public void testEqualsDifferentStart() {
    LocalDateTime differentStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    EventKey key2 = new EventKey("Subject", differentStart, end);
    assertFalse(key1.equals(key2));
  }

  @Test
  public void testEqualsDifferentEnd() {
    LocalDateTime differentEnd = LocalDateTime.of(2025, 1, 15, 12, 0);
    EventKey key2 = new EventKey("Subject", start, differentEnd);
    assertFalse(key1.equals(key2));
  }

  @Test
  public void testHashCodeEqualObjectsMatch() {
    EventKey key2 = new EventKey("Subject", start, end);
    assertEquals(key1.hashCode(), key2.hashCode());
  }

  @Test
  public void testHashCodeDifferentSubject() {
    EventKey key2 = new EventKey("Different Subject", start, end);
    assertFalse(key1.hashCode() == key2.hashCode());
  }

  @Test
  public void testHashCodeDifferentStart() {
    LocalDateTime differentStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    EventKey key2 = new EventKey("Subject", differentStart, end);
    assertFalse(key1.hashCode() == key2.hashCode());
  }

  @Test
  public void testHashCodeDifferentEnd() {
    LocalDateTime differentEnd = LocalDateTime.of(2025, 1, 15, 12, 0);
    EventKey key2 = new EventKey("Subject", start, differentEnd);
    assertFalse(key1.hashCode() == key2.hashCode());
  }
}
