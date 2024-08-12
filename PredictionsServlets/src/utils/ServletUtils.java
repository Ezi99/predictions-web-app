package utils;

import engine.world.management.WorldManagement;
import engine.world.management.WorldManagementImpl;
import jakarta.servlet.ServletContext;

public class ServletUtils {
    private static final String WORLD_MANAGEMENT_ATTRIBUTE_NAME = "worldManagement";
    private static final String ADMIN_EXISTS_ATTRIBUTE_NAME = "adminExists";
    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";


    private static final Object worldManagementLock = new Object();
    private static final Object adminExistsLock = new Object();
    private static final Object userManagerLock = new Object();

    public static WorldManagement getWorldManagement(ServletContext servletContext) {
        synchronized (worldManagementLock) {
            if (servletContext.getAttribute(WORLD_MANAGEMENT_ATTRIBUTE_NAME) == null) {
                WorldManagement worldManagement = new WorldManagementImpl();
                servletContext.setAttribute(WORLD_MANAGEMENT_ATTRIBUTE_NAME,  worldManagement);
            }
        }
        return (WorldManagement) servletContext.getAttribute(WORLD_MANAGEMENT_ATTRIBUTE_NAME);
    }

    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }


    public static boolean isAdminValid(ServletContext servletContext) {
        synchronized (adminExistsLock) {
            if (servletContext.getAttribute(ADMIN_EXISTS_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ADMIN_EXISTS_ATTRIBUTE_NAME, true);
                return true;
            } else {
                boolean exists = (boolean) servletContext.getAttribute(ADMIN_EXISTS_ATTRIBUTE_NAME);
                if(!exists){
                    servletContext.setAttribute(ADMIN_EXISTS_ATTRIBUTE_NAME, true);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static void logAdminOut(ServletContext servletContext){
        synchronized (adminExistsLock) {
            if (servletContext.getAttribute(ADMIN_EXISTS_ATTRIBUTE_NAME) != null) {
                servletContext.setAttribute(ADMIN_EXISTS_ATTRIBUTE_NAME, false);
            }
        }
    }
}
