package servlets.admin;

import com.google.gson.Gson;
import dto.requests.AllocationRequestsDTO;
import dto.requests.UpdateRequestStatusDTO;
import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;
import java.io.IOException;

@WebServlet(name = "AdminAllocationServlet", urlPatterns = {"/admin/allocations/requests"})
public class AdminAllocationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have an admin user to access allocations !";
        if(!SessionUtils.isAdminValid(request, response, message)){
            return;
        }

        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        AllocationRequestsDTO allocationRequests;
        Gson gson = new Gson();

        synchronized (getServletContext()) {
            allocationRequests = worldManagement.getAllocationRequests();
            response.getWriter().print(gson.toJson(allocationRequests));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have an admin user to access allocations !";
        if(!SessionUtils.isAdminValid(request, response, message)){
            return;
        }

        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        Gson gson = new Gson();
        UpdateRequestStatusDTO update = gson.fromJson(request.getReader(), UpdateRequestStatusDTO.class);

        synchronized (getServletContext()) {
            worldManagement.updateRequestStatus(update);
        }
    }
}
