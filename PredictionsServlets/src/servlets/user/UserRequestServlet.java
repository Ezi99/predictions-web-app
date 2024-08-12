package servlets.user;

import com.google.gson.Gson;
import dto.requests.RequestDTO;
import dto.requests.SubmitRequestDTO;
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

@WebServlet(name = "UserRequestServlet", urlPatterns = {"/request"})
public class UserRequestServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtils.getUsername(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("you're supposed to have a user name to access here !");
            return;
        }

        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        SubmitRequestDTO submitRequestDTO = gson.fromJson(request.getReader(), SubmitRequestDTO.class);


        synchronized (getServletContext()) {
            worldManagement.addAllocationRequest(submitRequestDTO);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userName = request.getParameter(Constants.USERNAME);

        if (SessionUtils.getUsername(request) == null || userName == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("you're supposed to have a user name to access here !");
            return;
        }

        Gson gson = new Gson();
        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        List<RequestDTO> requestDTOList;

        synchronized (getServletContext()) {
            requestDTOList = worldManagement.getUserRequests(userName);
            response.getWriter().print(gson.toJson(requestDTOList));
        }
    }
}
