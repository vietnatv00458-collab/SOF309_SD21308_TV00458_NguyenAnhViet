package servlet;

import utils.BannerImageUtil;
import utils.UploadPathUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@WebServlet("/promo-banners/*")
public class PromoBannerImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendRedirect(request.getContextPath() + "/images/promo-voucher-1.png");
            return;
        }

        String fileName = new File(pathInfo).getName();
        if (!BannerImageUtil.isSupportedImageName(fileName)) {
            response.sendRedirect(request.getContextPath() + "/images/promo-voucher-1.png");
            return;
        }

        File uploadDir = UploadPathUtil.bannerUploadDir().getCanonicalFile();
        File imageFile = new File(uploadDir, fileName).getCanonicalFile();

        if (!imageFile.getPath().startsWith(uploadDir.getPath()) || !imageFile.isFile()) {
            response.sendRedirect(request.getContextPath() + "/images/promo-voucher-1.png");
            return;
        }

        String contentType = getServletContext().getMimeType(imageFile.getName());
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Cache-Control", "public, max-age=31536000");
        Files.copy(imageFile.toPath(), response.getOutputStream());
    }
}
