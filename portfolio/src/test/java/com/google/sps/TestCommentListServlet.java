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


public class TestCommentListServlet {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private HttpServletRequest request;
  private HttpServletResponse response;
  private PrintWriter writer;
  private StringWriter stringWriter;

  @Before
  public void setUp() throws Exception {
    helper.setUp();

    this.request = mock(HttpServletRequest.class);
    this.response = mock(HttpServletResponse.class);

    this.stringWriter = new StringWriter();
    this.writer = new PrintWriter(this.stringWriter);
    when(this.response.getWriter()).thenReturn(this.writer);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEmpty() throws Exception {
    new CommentListServlet().doGet(request, response);
    writer.flush();
    assertTrue(stringWriter.toString().equals("[]"));
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
    assertTrue(stringWriter.toString().contains("a test comment"));
  }
}