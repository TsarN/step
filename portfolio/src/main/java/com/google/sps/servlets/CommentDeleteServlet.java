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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.*;
import com.google.sps.SafeParser;
import com.google.sps.UserManager;

/** Servlet that returns comments. */
@WebServlet("/commentDelete")
public class CommentDeleteServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String id = request.getParameter("id");
    if (id == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'id' parameter missing");
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      Key key = KeyFactory.stringToKey(id);

      if (!UserManager.canCurrentUserDeleteComment(key)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "not allowed to delete the comment");
        return;
      }

      datastore.delete(key);
    } catch (EntityNotFoundException exception) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "comment not found");
      return;
    } catch (IllegalArgumentException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'id' parameter is invalid");
      return;
    }

    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
