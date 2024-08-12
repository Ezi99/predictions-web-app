package servlets.admin;

import com.google.gson.Gson;
import dto.definition.WorldInfoDTO;
import engine.world.management.WorldManagement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "LoadFileServlet", urlPatterns = {"/admin/load/file"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class LoadFileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "you're supposed to have an admin user to upload simulations !";
        if(!SessionUtils.isAdminValid(request, response, message)){
            return;
        }

        WorldManagement worldManagement = ServletUtils.getWorldManagement(getServletContext());
        Gson gson = new Gson();

        try {
            Part part = request.getPart(Constants.FILE_NAME);
            synchronized (getServletContext()){
                WorldInfoDTO worldInfo = worldManagement.loadDataFile(part.getInputStream());
                response.getWriter().print(gson.toJson(worldInfo));
            }
        } catch (ServletException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("something went wrong when loading file");
        } catch (Exception e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
