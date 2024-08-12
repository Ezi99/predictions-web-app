package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class SessionUtils {

    public static String getUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static String getAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.ADMIN) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static void createAdminUser(HttpServletRequest request) {
        request.getSession(true).setAttribute(Constants.ADMIN, "admin");
    }

    public static void clearSession(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public static boolean isUserValid(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {

        if (SessionUtils.getUsername(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(message);
            return false;
        }

        return true;
    }

    public static boolean isAdminValid(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {

        if (SessionUtils.getAdminUser(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(message);
            return false;
        }

        return true;
    }

}