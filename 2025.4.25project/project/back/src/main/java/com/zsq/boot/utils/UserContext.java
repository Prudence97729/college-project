package com.zsq.boot.utils;

import com.zsq.boot.entity.User;

/***
 * 用户上下文工具类（基于Threadlocal）
 */
public class UserContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<User>();

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
    public static User getCurrentUser() {
        return currentUser.get();
    }
    public static void clearCurrentUser() {
        currentUser.remove();
    }
}
