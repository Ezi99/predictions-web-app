package servlets.admin;

import engine.world.management.WorldManagement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "SetThreadPoolServlet", urlPatterns = {"/threadPool"})
public class SetThreadPoolServlet extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have an admin user to set threadPool !";
        if(!SessionUtils.isAdminValid(request, response, message)){
            return;
        }

        try{
            WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
            BufferedReader reader = request.getReader();
            String param = reader.readLine().trim();
            int threadCount = Integer.parseInt(param);

            synchronized (getServletContext()){
                worldManagement.setThreadPool(threadCount);
            }

            response.getWriter().print("thread pool updated successfully !");
        } catch (NumberFormatException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("invalid thread count provided");
        } catch (Exception e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
