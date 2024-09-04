package pucmm.eict.proyectofinal.examguard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pucmm.eict.proyectofinal.examguard.dto.ChangePasswordDTO;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.User;
import pucmm.eict.proyectofinal.examguard.service.FolderService;
import pucmm.eict.proyectofinal.examguard.service.RecordingService;
import pucmm.eict.proyectofinal.examguard.service.UserService;

@Controller
public class HomeController {
    final UserService userService;
    final FolderService folderService;
    final RecordingService recordingService;

    @Autowired
    public HomeController(UserService userService, FolderService folderService, RecordingService recordingService) {
        this.userService = userService;
        this.folderService = folderService;
        this.recordingService = recordingService;
    }

    @GetMapping("home")
    public String page(Model model) {
        User loggedUser = userService.getLoggedUser();

        long cantRecordings = recordingService.countRecordingsByUser(loggedUser);
        long cantFolders = folderService.countFoldersByUser(loggedUser);
        long cantRecordingsProcessed = recordingService.countProcessedRecordingsByUser(loggedUser);

        model.addAttribute("cantRecordings", cantRecordings);
        model.addAttribute("cantFolders", cantFolders);
        model.addAttribute("cantRecordingsProcessed", cantRecordingsProcessed);
        model.addAttribute("user", loggedUser);
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());

        return "home";
    }
}
