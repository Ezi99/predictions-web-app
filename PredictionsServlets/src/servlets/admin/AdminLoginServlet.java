package servlets.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/admin/login"})
public class AdminLoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!ServletUtils.isAdminValid(getServletContext())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("server cannot serve more than one admin");
        } else {
            SessionUtils.createAdminUser(request);
            response.getWriter().print("logged in successfully !");
        }
    }
}
