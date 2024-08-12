package servlets.user;

import com.google.gson.Gson;
import dto.definition.TerminationDTO;
import dto.definition.WorldInfoDTO;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "TerminationServlet", urlPatterns = {"/simulation/termination"})
public class TerminationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have a user name to get termination !";
        if(!SessionUtils.isUserValid(request, response, message)){
            return;
        }


        Gson gson = new Gson();
        String worldID = request.getParameter(Constants.WORLD_ID);
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());

        try{
            checkValidity(worldID);
            TerminationDTO terminationDTO;
            synchronized (getServletContext()) {
                terminationDTO = worldManagement.getTerminationDTO(Integer.parseInt(worldID));
            }

            response.getWriter().print(gson.toJson(terminationDTO));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }

    }
    private void checkValidity(String worldID) {
        if (worldID == null) {
            String message = "you're supposed to provide worldID to get termination info !";
            throw new IllegalArgumentException(message);
        }

        try {
            Integer.parseInt(worldID);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid requestID");
        }

    }

}
