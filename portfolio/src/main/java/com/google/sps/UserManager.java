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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UserManager {
  public static String getCurrentUserId() {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      return null;
    }

    return userService.getCurrentUser().getUserId();
  }

  /**
   * Returns Entity object representing user with specified id.
   * Creates the Entity in the Datastore if necessary.
   * If id is null, returns null.
   *
   * @param id user's id
   * @return Entity object representing the user
   */
  public static Entity getUser(String id) {
    if (id == null) {
      return null;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("user")
        .setFilter(new Query.FilterPredicate(
            "id", Query.FilterOperator.EQUAL, id));

    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    if (entity != null) {
      return entity;
    }

    entity = new Entity("user");
    entity.setProperty("id", id);
    entity.setProperty("nickname", "");
    datastore.put(entity);

    return entity;
  }

  public static Entity getCurrentUser() {
    return getUser(getCurrentUserId());
  }

  public static String getCurrentUserNickname() {
    Entity user = getCurrentUser();
    return (String)user.getProperty("nickname");
  }

  public static void setCurrentUserNickname(String nickname) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity user = getCurrentUser();
    user.setProperty("nickname", nickname);
    datastore.put(user);
  }

  public static boolean canCurrentUserDeleteComment(Key key) throws EntityNotFoundException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn() && userService.isUserAdmin()) {
      return true;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity comment = datastore.get(key);

    return comment.getProperty("authorId").equals(getCurrentUserId());
  }
}
