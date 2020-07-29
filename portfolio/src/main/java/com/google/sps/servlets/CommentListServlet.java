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

package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.sps.Comment;
import com.google.sps.SafeParser;
import com.google.sps.Translator;

/** Servlet that returns comments. */
@WebServlet("/commentList")
public class CommentListServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/json");

    long amount = SafeParser.parseInt(request.getParameter("amount"), -1);

    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.create();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("comment");

    String order = request.getParameter("order");

    if (order == null) {
      order = "newest";
    }

    switch (order) {
      case "oldest": // oldest first
        query.addSort("timestamp", SortDirection.ASCENDING);
        break;

      case "newest": // newest first
        query.addSort("timestamp", SortDirection.DESCENDING);
        break;

      default:
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'order' must be either 'oldest' or 'newest'");
        return;
    }

    String translateInto = request.getParameter("translateInto");

    PreparedQuery results = datastore.prepare(query);
    
    List<Comment> comments = new ArrayList<>();

    Translator translator = new Translator();

    for (Entity entity : results.asIterable()) {
      if (comments.size() == amount) {
        break;
      }

      String text = (String) entity.getProperty("text");

      try {
        text = translator.translate(text, translateInto);
      } catch (TranslateException exception) {
        response.sendError(exception.getCode(), "translation error: " + exception.getMessage());
        return;
      }

      comments.add(new Comment(
        KeyFactory.keyToString(entity.getKey()),
        (String)entity.getProperty("author"),
        (String)entity.getProperty("authorId"),
        text,
        Instant.ofEpochMilli((long)entity.getProperty("timestamp"))
      ));
    }

    String json = gson.toJson(comments);
    response.getWriter().write(json);
  }
}
