package servlets.user;

import com.google.gson.Gson;
import dto.SimulationControl;
import dto.definition.ActiveEnvironmentDTO;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "SimulationControlServlet", urlPatterns = {"/simulation/control"})
public class SimulationControlServlet extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have a user name to control simulation !";
        if(!SessionUtils.isUserValid(request, response, message)){
            return;
        }

        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        String control = request.getParameter(Constants.CONTROL);
        String worldID = request.getParameter(Constants.WORLD_ID);

        synchronized (getServletContext()) {
            try {
                int id = checkValidity(control, worldID);
                if (control.equals(SimulationControl.STOP.name())) {
                    worldManagement.stopSimulation(id);
                } else if (control.equals(SimulationControl.PAUSE.name())) {
                    worldManagement.pauseSimulation(id);
                } else if (control.equals(SimulationControl.RESUME.name())) {
                    worldManagement.resumeSimulation(id);
                } else {
                    throw new IllegalArgumentException("invalid control type");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
            }
        }
    }

    private int checkValidity(String control, String worldID) {
        if (control == null || worldID == null) {
            String message = "you're supposed to provide worldID and control type to control simulation !";
            throw new IllegalArgumentException(message);
        }

        try {
            return Integer.parseInt(worldID);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid world ID provided");
        }
    }
}
