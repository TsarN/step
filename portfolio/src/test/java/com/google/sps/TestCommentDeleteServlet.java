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

import com.google.appengine.api.datastore.*;
import com.google.sps.servlets.CommentDeleteServlet;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCommentDeleteServlet extends ServletTest {
  @Test
  public void testUnauthenticatedDelete() throws Exception {
    helper.setEnvIsLoggedIn(false);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment = new Entity("comment");
    comment.setProperty("text", "something something");
    comment.setProperty("author", "someone");
    comment.setProperty("authorId", "not_test_user");
    comment.setProperty("timestamp", 123456789L);

    datastore.put(comment);

    when(request.getParameter("id")).thenReturn(KeyFactory.keyToString(comment.getKey()));
    new CommentDeleteServlet().doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "not allowed to delete the comment");

    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results.toArray(), arrayContaining(comment));
  }

  @Test
  public void testDeleteOtherUserComment() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment = new Entity("comment");
    comment.setProperty("text", "something something");
    comment.setProperty("author", "someone");
    comment.setProperty("authorId", "not_test_user");
    comment.setProperty("timestamp", 123456789L);

    datastore.put(comment);

    when(request.getParameter("id")).thenReturn(KeyFactory.keyToString(comment.getKey()));
    new CommentDeleteServlet().doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "not allowed to delete the comment");

    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results.toArray(), arrayContaining(comment));
  }

  @Test
  public void testAdminDelete() throws Exception {
    helper.setEnvIsAdmin(true);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment = new Entity("comment");
    comment.setProperty("text", "something something");
    comment.setProperty("author", "someone");
    comment.setProperty("authorId", "not_test_user");
    comment.setProperty("timestamp", 123456789L);

    datastore.put(comment);

    when(request.getParameter("id")).thenReturn(KeyFactory.keyToString(comment.getKey()));
    new CommentDeleteServlet().doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results.size(), equalTo(0));
  }

  @Test
  public void testNonAdminDelete() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment1 = new Entity("comment");
    comment1.setProperty("text", "something something");
    comment1.setProperty("author", "someone");
    comment1.setProperty("authorId", "not_test_user");
    comment1.setProperty("timestamp", 1234567890L);

    Entity comment2 = new Entity("comment");
    comment2.setProperty("text", "i want to delete this comment");
    comment2.setProperty("author", "me");
    comment2.setProperty("authorId", "test_user");
    comment2.setProperty("timestamp", 1234567891L);

    datastore.put(comment1);
    datastore.put(comment2);

    when(request.getParameter("id")).thenReturn(KeyFactory.keyToString(comment2.getKey()));
    new CommentDeleteServlet().doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results.toArray(), arrayContaining(comment1));
  }
}
