package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.UploadPathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@WebServlet("/uploads/*")
public class UploadedImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = new File(pathInfo).getName();
        File imageFile = new File(UploadPathUtil.productUploadDir(), fileName);
        File uploadDir = UploadPathUtil.productUploadDir().getCanonicalFile();
        File canonicalImageFile = imageFile.getCanonicalFile();

        if (!canonicalImageFile.getPath().startsWith(uploadDir.getPath())
                || !canonicalImageFile.isFile()) {
            response.sendRedirect(request.getContextPath() + "/images/product-boba-milk-tea.png");
            return;
        }

        String contentType = getServletContext().getMimeType(canonicalImageFile.getName());
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Cache-Control", "public, max-age=31536000");
        Files.copy(canonicalImageFile.toPath(), response.getOutputStream());
    }
}
