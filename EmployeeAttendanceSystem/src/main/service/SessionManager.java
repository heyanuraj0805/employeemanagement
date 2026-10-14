package main.service;

import main.model.User;

/**
 * Singleton that holds the currently-authenticated user for the session.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser()          { return currentUser;      }
    public boolean isLoggedIn()           { return currentUser != null; }

    public boolean isAdmin()   { return isLoggedIn() && currentUser.getRole() == User.Role.ADMIN;   }
    public boolean isManager() { return isLoggedIn() &&
        (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER); }

    public void logout() { currentUser = null; }
}
