package servlets;

import com.google.gson.Gson;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "WorldInfoServlet", urlPatterns = {"/world/info"})
public class WorldInfoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtils.getUsername(request) == null && SessionUtils.getAdminUser(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("you're supposed to have a user name to get world details !");
            return;
        }

        Gson gson = new Gson();
        String simulationName = request.getParameter("simulationName");

        if (simulationName == null) {
            simulationName = "";
        }
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());

        if(!simulationName.isEmpty()){
            synchronized (getServletContext()){
                try {
                    response.getWriter().print(gson.toJson(worldManagement.getWorldInfo(simulationName)));
                } catch (Exception e){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print(e.getMessage());
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("no simulation name was inserted");
        }
    }
}
