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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json");

        UserService userService = UserServiceFactory.getUserService();

        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        builder.enableComplexMapKeySerialization();
        Gson gson = builder.create();

        Map<String, Object> res = new HashMap<>();

        if (userService.isUserLoggedIn()) {
            String email = userService.getCurrentUser().getEmail();
            String logoutUrl = userService.createLogoutURL("/");

            res.put("loggedIn", true);
            res.put("email", email);
            res.put("logoutUrl", logoutUrl);
        } else {
            String loginUrl = userService.createLoginURL("/");

            res.put("loggedIn", false);
            res.put("loginUrl", loginUrl);
        }

        response.getWriter().println(gson.toJson(res));
    }
}
