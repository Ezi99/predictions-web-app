package servlets.execution;

import com.google.gson.Gson;
import dto.execution.start.StartSimulationDTO;
import engine.execution.SimulationState;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SimulationStateServlet", urlPatterns = {"/simulation/state"})
public class SimulationStateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtils.getUsername(request) == null && SessionUtils.getAdminUser(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("you're supposed to have a user name to access here !");
            return;
        }

        getSimulationsState(request, response);
    }

    private void getSimulationsState(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        String worldID = request.getParameter(Constants.WORLD_ID);
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());

        if (worldID != null && worldID.equals("all")) {
            List<StartSimulationDTO> simulationsState;
            String username = SessionUtils.getUsername(request);

            synchronized (getServletContext()) {
                if (SessionUtils.getUsername(request) != null) {
                    simulationsState = worldManagement.getUserSimulationsState(username);
                } else {
                    simulationsState = worldManagement.getSimulationsState();
                }
            }

            response.getWriter().print(gson.toJson(simulationsState));
        } else {
            try {
                int id = checkValidity(worldID);
                SimulationState simulationState;

                synchronized (getServletContext()) {
                    simulationState = worldManagement.getSimulationState(id);
                }

                response.getWriter().print(simulationState.name());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
            }
        }

    }

    private int checkValidity(String worldID) {
        if (worldID == null) {
            String message = "you're supposed to provide worldID to get simulation state !";
            throw new IllegalArgumentException(message);
        }

        try {
            return Integer.parseInt(worldID);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid requestID");
        }

    }
}
