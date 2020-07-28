package com.google.sps;

import com.google.appengine.api.datastore.*;
import org.junit.Test;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.*;

public class TestStateLeaks extends ServletTest {
  // Run this test twice to prove we're not leaking any state across tests.
  private void doTest() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
    ds.put(new Entity("yam"));
    ds.put(new Entity("yam"));
    assertEquals(2, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
  }

  @Test
  public void testInsert1() {
    doTest();
  }

  @Test
  public void testInsert2() {
    doTest();
  }
}
