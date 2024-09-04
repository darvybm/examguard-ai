package pucmm.eict.proyectofinal.examguard.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            return switch (statusCode) {
                case 400 -> "error/400";
                case 401 -> "error/401";
                case 403 -> "error/403";
                case 404 -> "error/404";
                case 500 -> "error/500";
                default -> "error/500";
            };
        }

        return "error/500";
    }
}
