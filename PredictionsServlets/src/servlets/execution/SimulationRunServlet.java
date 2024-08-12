package servlets.execution;

import com.google.gson.Gson;
import dto.definition.WorldInfoDTO;
import dto.execution.SimulationDTO;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "SimulationRunServlet", urlPatterns = {"/simulation/run"})
public class SimulationRunServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have a user name to start simulation !";
        if(!SessionUtils.isUserValid(request, response, message)){
            return;
        }

        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        WorldInfoDTO worldInfoDTO = gson.fromJson(request.getReader(), WorldInfoDTO.class);
        String requestNumber = request.getParameter(Constants.REQUEST_ID);
        String userName = SessionUtils.getUsername(request);

        try {
            isStartSimulationValid(worldInfoDTO, requestNumber);
            SimulationDTO simulationDTO;
            int id = Integer.parseInt(requestNumber);
            synchronized (getServletContext()) {
                simulationDTO = worldManagement.startSimulation(worldInfoDTO, userName, id);
            }

            response.getWriter().print(gson.toJson(simulationDTO));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(SessionUtils.getUsername(request) == null && SessionUtils.getAdminUser(request) == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("you're supposed to have a user name to get simulation walkthrough !");
            return;
        }

        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        String walkthroughType = request.getParameter(Constants.WALKTHROUGH_TYPE);
        String worldID = request.getParameter(Constants.WORLD_ID);
        SimulationDTO simulationDTO;

        synchronized (getServletContext()) {
            try {
                isWalkthroughRequestValid(walkthroughType, worldID);
                if (walkthroughType.equals("runningResults")) {
                    simulationDTO = worldManagement.getSimulationWalkthrough(Integer.parseInt(worldID));
                } else {
                    simulationDTO = worldManagement.getSimulationEndResults(Integer.parseInt(worldID));
                }

                response.getWriter().print(gson.toJson(simulationDTO));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
            }
        }
    }

    private void isStartSimulationValid(WorldInfoDTO worldInfoDTO, String requestNumber) {
        if (worldInfoDTO == null || requestNumber == null) {
            String message = "you're supposed to provide an active environment and request number to start !";
            throw new IllegalArgumentException(message);
        }

        try {
            Integer.parseInt(requestNumber);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid requestID");
        }
    }

    private void isWalkthroughRequestValid(String walkThroughType, String worldID) {
        if (walkThroughType == null || worldID == null) {
            String message = "you need to provide a walkthrough Type and world ID to get current stats !";
            throw new IllegalArgumentException(message);
        }

        if (!walkThroughType.equals("endResults") && !walkThroughType.equals("runningResults")) {
            throw new IllegalArgumentException("invalid walkthrough type");
        }

        try {
            Integer.parseInt(worldID);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid world ID");
        }
    }
}
