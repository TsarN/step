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
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import com.google.sps.servlets.CommentListServlet;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;


public class TestCommentListServlet extends ServletTest {
  @Test
  public void testEmpty() throws Exception {
    when(request.getParameter("order")).thenReturn("newest");
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
    when(request.getParameter("order")).thenReturn("newest");

    new CommentListServlet().doGet(request, response);
    writer.flush();
    String response = stringWriter.toString();
    assertTrue(response.contains("a test comment"));
    assertTrue(response.contains("anonymous"));
  }

  @Test
  public void testCommentTranslation() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity commentEntity = new Entity("comment");
    commentEntity.setProperty("text", "Africa"); // A word that only has one translation
    commentEntity.setProperty("author", "do not translate my name");
    commentEntity.setProperty("timestamp", 123456789L);

    datastore.put(commentEntity);
    when(request.getParameter("order")).thenReturn("newest");
    when(request.getParameter("translateInto")).thenReturn("ru");

    new CommentListServlet().doGet(request, response);
    writer.flush();
    String response = stringWriter.toString();
    assertTrue(response.contains("do not translate my name"));
    assertTrue(response.contains("Африка"));
  }

  @Test
  public void testCommentTranslationInvalidLanguage() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity commentEntity = new Entity("comment");
    commentEntity.setProperty("text", "Africa");
    commentEntity.setProperty("author", "do not translate my name");
    commentEntity.setProperty("timestamp", 123456789L);

    datastore.put(commentEntity);
    when(request.getParameter("order")).thenReturn("newest");
    when(request.getParameter("translateInto")).thenReturn("invalid-language-code");

    new CommentListServlet().doGet(request, response);

    verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("translation error"));
  }

  @Test
  public void testCommentOrderNewest() throws Exception {
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

    Entity comment4 = new Entity("comment");
    comment4.setProperty("text", "four");
    comment4.setProperty("author", "david");
    comment4.setProperty("timestamp", 1_000_000_000L);
    datastore.put(comment4);

    when(request.getParameter("amount")).thenReturn("3");
    when(request.getParameter("order")).thenReturn("newest");
    new CommentListServlet().doGet(request, response);
    writer.flush();
    String response = stringWriter.toString();
    assertTrue(response.contains("one"));
    assertTrue(response.contains("two"));
    assertTrue(response.contains("three"));
    assertFalse(response.contains("four"));

    assertTrue(response.indexOf("one") > response.indexOf("three"));
    assertTrue(response.indexOf("three") > response.indexOf("two"));
  }


  @Test
  public void testCommentOrderOldest() throws Exception {
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

    Entity comment4 = new Entity("comment");
    comment4.setProperty("text", "four");
    comment4.setProperty("author", "david");
    comment4.setProperty("timestamp", 1_000_000_000L);
    datastore.put(comment4);

    when(request.getParameter("amount")).thenReturn("3");
    when(request.getParameter("order")).thenReturn("oldest");
    new CommentListServlet().doGet(request, response);
    writer.flush();
    String response = stringWriter.toString();
    assertTrue(response.contains("one"));
    assertFalse(response.contains("two"));
    assertTrue(response.contains("three"));
    assertTrue(response.contains("four"));

    assertTrue(response.indexOf("one") < response.indexOf("three"));
    assertTrue(response.indexOf("four") < response.indexOf("one"));
  }
}