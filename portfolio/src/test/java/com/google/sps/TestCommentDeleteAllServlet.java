package com.google.sps;

import com.google.appengine.api.datastore.*;
import com.google.sps.servlets.CommentDeleteAllServlet;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCommentDeleteAllServlet extends ServletTest {
  @Test
  public void testAdminDelete() throws Exception {
    helper.setEnvIsAdmin(true);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment1 = new Entity("comment");
    comment1.setProperty("text", "something something");
    comment1.setProperty("author", "someone");
    comment1.setProperty("authorId", "not_test_user");
    comment1.setProperty("timestamp", 123456789L);

    Entity comment2 = new Entity("comment");
    comment2.setProperty("text", "something else something else");
    comment2.setProperty("author", "someone else");
    comment2.setProperty("authorId", "not_test_user_again");
    comment2.setProperty("timestamp", 987654321L);

    datastore.put(comment1);
    datastore.put(comment2);

    new CommentDeleteAllServlet().doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results, empty());
  }

  @Test
  public void testNonAdminDelete() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment1 = new Entity("comment");
    comment1.setProperty("text", "something something");
    comment1.setProperty("author", "someone");
    comment1.setProperty("authorId", "not_test_user");
    comment1.setProperty("timestamp", 123456789L);

    Entity comment2 = new Entity("comment");
    comment2.setProperty("text", "something else something else");
    comment2.setProperty("author", "someone else");
    comment2.setProperty("authorId", "not_test_user_again");
    comment2.setProperty("timestamp", 987654321L);

    datastore.put(comment1);
    datastore.put(comment2);

    new CommentDeleteAllServlet().doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "admin required");
    Query query = new Query("comment");
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertThat(results, containsInAnyOrder(comment1, comment2));
  }
}
