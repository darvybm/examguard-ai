package pucmm.eict.proyectofinal.examguard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("")
    public String page() {
        return "page";
    }
}
