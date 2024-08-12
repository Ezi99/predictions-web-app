package servlets.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.SessionUtils;
import java.io.IOException;
import utils.ServletUtils;
import utils.UserManager;

@WebServlet(name = "UserLoginServlet", urlPatterns = {"/user/login"})
public class UserLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) {
            String usernameFromParameter = request.getParameter(Constants.USERNAME);
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("no userName inserted for login");
            } else {
                usernameFromParameter = usernameFromParameter.trim();
                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        String errorMessage = "Username " + usernameFromParameter + " already exists. Please enter a different username.";
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().print(errorMessage);

                    }
                    else {
                        userManager.addUser(usernameFromParameter);
                        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().print("signed in successfully !");

                    }
                }
            }
        }
    }
}
