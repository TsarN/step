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
}
