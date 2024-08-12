package servlets.user;

import com.google.gson.Gson;
import dto.definition.ActiveEnvironmentDTO;
import dto.definition.WorldInfoDTO;
import engine.world.World;
import engine.world.WorldImpl;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "ActiveEnvironmentServlet", urlPatterns = {"/activeEnvironment"})
public class ActiveEnvironmentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have a user name to get active environment !";
        if (!SessionUtils.isUserValid(request, response, message)) {
            return;
        }

        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        String worldID = request.getParameter(Constants.WORLD_ID);

        synchronized (getServletContext()) {
            try {
                int id = Integer.parseInt(worldID);
                ActiveEnvironmentDTO activeEnv = worldManagement.getActiveEnvironmentAndPopulation(id);
                response.getWriter().print(gson.toJson(activeEnv));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
            }
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have a user name to set active environment !";
        if (!SessionUtils.isUserValid(request, response, message)) {
            return;
        }

        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        ActiveEnvironmentDTO activeEnvDTO;
        World world = new WorldImpl(-1, null, -1);

        synchronized (getServletContext()) {
            try {
                WorldInfoDTO worldInfoDTO = gson.fromJson(request.getReader(), WorldInfoDTO.class);
                activeEnvDTO = worldManagement.instantiateWorld(worldInfoDTO, world);
                response.getWriter().print(gson.toJson(activeEnvDTO));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
            }
        }
    }
}
