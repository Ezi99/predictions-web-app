package servlets;

import engine.world.World;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;
import utils.UserManager;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getSession() != null) {
            if (request.getSession().getAttribute(Constants.ADMIN) != null) {
                ServletUtils.logAdminOut(getServletContext());
            } else if (request.getSession().getAttribute(Constants.USERNAME) != null) {
                String usernameFromSession = SessionUtils.getUsername(request);
                UserManager userManager = ServletUtils.getUserManager(getServletContext());

                if (usernameFromSession != null) {
                    System.out.println("Clearing session for " + usernameFromSession);
                    userManager.removeUser(usernameFromSession);
                    WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
                    synchronized (getServletContext()){
                        worldManagement.removeUserRequests(usernameFromSession);
                    }
                }
            }
            SessionUtils.clearSession(request);
        }
    }
}
