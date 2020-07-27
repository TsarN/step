package com.google.sps;

import com.google.appengine.api.datastore.*;
import com.google.sps.servlets.CommentPostServlet;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestCommentPostServlet extends ServletTest {
    @Test
    public void testSubmit() throws Exception {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("author")).thenReturn("fred");
        when(request.getParameter("comment")).thenReturn("a test comment");

        new CommentPostServlet().doPost(request, response);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);

        Query query = new Query("comment");
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertEquals(1, results.size());
        assertEquals("fred", results.get(0).getProperty("author"));
        assertEquals("a test comment", results.get(0).getProperty("text"));
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
        assertEquals(0, results.size());
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
        assertEquals(0, results.size());
    }
}
