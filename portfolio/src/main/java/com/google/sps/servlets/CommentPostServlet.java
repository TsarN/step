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
import java.time.Instant;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.UserManager;

/** Servlet that returns comments. */
@WebServlet("/commentPost")
public class CommentPostServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      // It's not really "forbidden", more like "not authenticated"
      // Sadly 401 is for HTTP-style authentication, which we don't want
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "authentication required");
    }

    String comment = request.getParameter("comment");
    String author = request.getParameter("author");

    if (comment == null || comment.trim().isEmpty()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'comment' parameter must not be empty");
      return;
    }

    if (author == null || author.trim().isEmpty()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'author' parameter must not be empty");
      return;
    }

    Entity commentEntity = new Entity("comment");
    commentEntity.setProperty("text", comment);
    commentEntity.setProperty("author", author);
    commentEntity.setProperty("authorId", UserManager.getCurrentUserId());
    commentEntity.setProperty("timestamp", Instant.now().toEpochMilli());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Remember the nickname
    UserManager.setCurrentUserNickname(author);

    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
