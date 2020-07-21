// Copyright 2019 Google LLC
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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Servlet that returns comments. */
@WebServlet("/commentList")
public class CommentListServlet extends HttpServlet {
  private static int parseInt(String string, int fallback) {
    if (string == null) {
      return fallback;
    }

    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/json");

    int amount = parseInt(request.getParameter("amount"), -1);

    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();
    Gson gson = builder.create();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    
    List<String> comments = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      if (comments.size() == amount) {
        break;
      }
      comments.add((String)entity.getProperty("text"));
    }

    String json = gson.toJson(comments);
    response.getWriter().write(json);
  }
}
