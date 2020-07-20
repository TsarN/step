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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/commentsData")
public class DataServlet extends HttpServlet {
  private static List<String> comments;
  
  static {
    comments = new ArrayList<>();
    comments.add("That's an interesting website you got here");
    comments.add("Maybe you could post a link to your youtube channel??");
    comments.add("А мне вот интересно: работает ли юникод корректно?");
    comments.add("And what if I use characters outside of 𝔹𝕄ℙ? 😳");
    comments.add("<img src=x onerror=alert(1)>");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/json");

    GsonBuilder builder = new GsonBuilder();
    builder.disableHtmlEscaping();

    Gson gson = builder.create();
    String json = gson.toJson(comments);
    response.getWriter().write(json);
  }
}
