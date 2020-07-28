package com.google.sps;

import com.google.appengine.api.datastore.*;
import com.google.sps.servlets.CommentPostServlet;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestCommentPostServlet extends ServletTest {
  @Test
  public void testSubmitNotAuthenticated() throws Exception {
    helper.setEnvIsLoggedIn(false);

    when(request.getParameter("author")).thenReturn("anonymous user");
    when(request.getParameter("comment")).thenReturn("a test comment");

    new CommentPostServlet().doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "authentication required");
  }

  @Test
  public void testSubmit() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter("author")).thenReturn("fred");
    when(request.getParameter("comment")).thenReturn("a test comment");

    Instant instant = Instant.ofEpochMilli(1234567890L);

    CommentPostServlet servlet = new CommentPostServlet();
    servlet.setMockInstant(instant);
    servlet.doPost(request, response);
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);

    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    Entity expected = new Entity("comment");
    expected.setProperty("author", "fred");
    expected.setProperty("text", "a test comment");
    expected.setProperty("authorId", "test_user");
    expected.setProperty("timestamp", instant.toEpochMilli());

    assertThat(results.stream().map(PropertyContainer::getProperties).toArray(),
        arrayContaining(expected.getProperties()));

    assertThat(UserManager.getCurrentUserNickname(), equalTo("fred"));
  }

  @Test
  public void testNoName() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter("author")).thenReturn(null);
    when(request.getParameter("comment")).thenReturn("a test comment");
    new CommentPostServlet().doPost(request, response);
    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, "'author' parameter must not be empty"
    );

    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results.size(), equalTo(0));
  }

  @Test
  public void testNoText() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    when(request.getParameter("author")).thenReturn("fred");
    when(request.getParameter("comment")).thenReturn(null);
    new CommentPostServlet().doPost(request, response);
    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, "'comment' parameter must not be empty"
    );

    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results.size(), equalTo(0));
  }
}
