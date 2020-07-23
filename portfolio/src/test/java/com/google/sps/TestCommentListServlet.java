// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.google.sps.servlets.CommentListServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class TestCommentListServlet extends ServletTest {
  @Test
  public void testEmpty() throws Exception {
    new CommentListServlet().doGet(request, response);
    writer.flush();
    assertEquals("[]", stringWriter.toString());
  }

  @Test
  public void testSingleComment() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity commentEntity = new Entity("comment");
    commentEntity.setProperty("text", "a test comment");
    commentEntity.setProperty("author", "anonymous");
    commentEntity.setProperty("timestamp", 123456789L);

    datastore.put(commentEntity);

    new CommentListServlet().doGet(request, response);
    writer.flush();
    String response = stringWriter.toString();
    assertTrue(response.contains("a test comment"));
    assertTrue(response.contains("anonymous"));
  }

  @Test
  public void testCommentOrder() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment1 = new Entity("comment");
    comment1.setProperty("text", "one");
    comment1.setProperty("author", "alice");
    comment1.setProperty("timestamp", 1_000_001_000L);
    datastore.put(comment1);

    Entity comment2 = new Entity("comment");
    comment2.setProperty("text", "two");
    comment2.setProperty("author", "bob");
    comment2.setProperty("timestamp", 1_000_003_000L);
    datastore.put(comment2);

    Entity comment3 = new Entity("comment");
    comment3.setProperty("text", "three");
    comment3.setProperty("author", "charlie");
    comment3.setProperty("timestamp", 1_000_002_000L);
    datastore.put(comment3);

    new CommentListServlet().doGet(request, response);
    writer.flush();
    String response = stringWriter.toString();
    assertTrue(response.contains("one"));
    assertTrue(response.contains("two"));
    assertTrue(response.contains("three"));

    assertTrue(response.indexOf("one") > response.indexOf("three"));
    assertTrue(response.indexOf("three") > response.indexOf("two"));
  }
}