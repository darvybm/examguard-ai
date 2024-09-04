package pucmm.eict.proyectofinal.examguard.controller;

import com.sendgrid.helpers.mail.Mail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pucmm.eict.proyectofinal.examguard.dto.FolderDTO;
import pucmm.eict.proyectofinal.examguard.model.Event;
import pucmm.eict.proyectofinal.examguard.model.Folder;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.service.FolderService;
import pucmm.eict.proyectofinal.examguard.service.MailService;
import pucmm.eict.proyectofinal.examguard.service.RecordingService;
import pucmm.eict.proyectofinal.examguard.service.UserService;

import java.util.*;

@Controller
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final UserService userService;
    private final MailService mailService;

    @GetMapping("")
    public String listFolders(Model model) {
        model.addAttribute("folders", folderService.getFoldersByUser(userService.getLoggedUser()));
        return "folder/list";
    }

    @GetMapping("/{folderId}")
    public String folderDetails(@PathVariable ObjectId folderId, Model model) {
        Folder folder = folderService.getFolderById(folderId, userService.getLoggedUser())
                .orElseThrow(() -> new IllegalArgumentException("Invalid folder Id:" + folderId));

        // Procesar los datos para los gráficos de pastel y radar
        Map<String, Integer> pieChartDataMap = new HashMap<>();
        Map<String, Integer> radarChartDataMap = new HashMap<>();

        // Iterar sobre las grabaciones en la carpeta y procesar eventos para los gráficos
        for (Recording recording : folder.getRecordings()) {
            for (Event event : recording.getEvents()) {
                switch (event.getEventType()) {
                    // Para el gráfico radar
                    case HEAD_POSE_DOWN:
                    case HEAD_POSE_FORWARD:
                    case HEAD_POSE_LEFT:
                    case HEAD_POSE_RIGHT:
                    case HEAD_POSE_UNKNOWN:
                    case HEAD_POSE_UP:
                        radarChartDataMap.put(event.getEventType().toString(), radarChartDataMap.getOrDefault(event.getEventType().toString(), 0) + 1);
                        break;
                    // Para el gráfico de pastel
                    default:
                        if (recording.getEventsToDetect().contains(event.getEventType())) {
                            pieChartDataMap.put(event.getEventType().toString(), pieChartDataMap.getOrDefault(event.getEventType().toString(), 0) + 1);
                        }
                        break;
                }
            }
        }

        // Convertir los mapas a listas para pasar al modelo
        List<String> pieChartLabels = new ArrayList<>(pieChartDataMap.keySet());
        List<Integer> pieChartData = new ArrayList<>(pieChartDataMap.values());

        List<String> radarChartLabels = new ArrayList<>(radarChartDataMap.keySet());
        List<Integer> radarChartData = new ArrayList<>(radarChartDataMap.values());

        // Agregar los atributos al modelo
        model.addAttribute("folder", folder);
        model.addAttribute("pieChartLabels", pieChartLabels);
        model.addAttribute("pieChartData", pieChartData);
        model.addAttribute("radarChartLabels", radarChartLabels);
        model.addAttribute("radarChartData", radarChartData);

        return "folder/details";
    }


    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@Valid @RequestBody FolderDTO folderDTO, BindingResult result) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Folder folder = new Folder();
            folder.setId(new ObjectId());
            folder.setName(folderDTO.getName());
            folder.setDescription(folderDTO.getDescription());
            folder.setUser(userService.getLoggedUser()); // Asumiendo que obtienes al usuario logueado

            folderService.createFolder(folder, userService.getLoggedUser());

            return ResponseEntity.status(HttpStatus.CREATED).body("Folder creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el folder");
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editFolder(@PathVariable ObjectId id, @Valid @RequestBody FolderDTO folderDTO, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Folder> optionalFolder = folderService.getFolderById(id, userService.getLoggedUser());

        if (optionalFolder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder no encontrado");
        }

        try {
            Folder folder = optionalFolder.get();
            folder.setName(folderDTO.getName());
            folder.setDescription(folderDTO.getDescription());
            folder.setUser(userService.getLoggedUser());

            folderService.updateFolder(folder,userService.getLoggedUser());

            return ResponseEntity.ok("Folder actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el folder");
        }
    }

    @DeleteMapping("/delete/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable ObjectId folderId) {
        try {
            folderService.deleteFolder(folderId, userService.getLoggedUser());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/send-email/{folderId}")
    public ResponseEntity<?> sendEmail(@PathVariable ObjectId folderId) {
        try {
            Folder folder = folderService.getFolderById(folderId, userService.getLoggedUser())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

            folder.getRecordings().stream()
                    .filter(recording -> recording.isProcessed() && recording.getStudent() != null)
                    .forEach(recording -> mailService.sendFraudEmail(userService.getLoggedUser(), recording, folder));

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
