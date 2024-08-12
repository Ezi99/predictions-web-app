package servlets.execution;

import com.google.gson.Gson;
import dto.definition.SimulationListDTO;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "SimulationListServlet", urlPatterns = {"/simulation/list"})
public class SimulationListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(SessionUtils.getUsername(request) == null && SessionUtils.getAdminUser(request) == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("you're supposed to have a user name to access here !");
            return;
        }

        SimulationListDTO simulationList;
        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());

        synchronized (getServletContext()){
            simulationList = worldManagement.getSimulationList();
        }

        response.getWriter().print(gson.toJson(simulationList));
    }
}
