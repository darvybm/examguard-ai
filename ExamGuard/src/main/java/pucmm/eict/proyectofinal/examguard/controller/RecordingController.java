package pucmm.eict.proyectofinal.examguard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pucmm.eict.proyectofinal.examguard.dto.FileDTO;
import pucmm.eict.proyectofinal.examguard.dto.RecordingProcessDTO;
import pucmm.eict.proyectofinal.examguard.exception.RecordingNotFoundException;
import pucmm.eict.proyectofinal.examguard.model.Event;
import pucmm.eict.proyectofinal.examguard.model.Folder;
import pucmm.eict.proyectofinal.examguard.model.Recording;
import pucmm.eict.proyectofinal.examguard.model.User;
import pucmm.eict.proyectofinal.examguard.service.FolderService;
import pucmm.eict.proyectofinal.examguard.service.GridFsService;
import pucmm.eict.proyectofinal.examguard.service.RecordingService;
import pucmm.eict.proyectofinal.examguard.service.UserService;
import pucmm.eict.proyectofinal.examguard.utils.AnimatedGifEncoder;
import pucmm.eict.proyectofinal.examguard.utils.DataTableResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/recordings")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingService recordingService;
    private final FolderService folderService;
    private final GridFsService gridFsService;
    private final UserService userService;

    @GetMapping("")
    public String recordingList() {
        return "recording/list";
    }

    // Obtener estadísticas generales de las grabaciones
    @GetMapping("/all-statistics")
    public ResponseEntity<Map<String, Object>> recordingStatistics(Model model) {
        User loggedUser = userService.getLoggedUser();
        List<Recording> recordings = recordingService.getRecordingsByUser(loggedUser);

        double totalFraudPercentage = recordings.stream().mapToDouble(Recording::getFraudPercentage).average().orElse(0.0);
        int totalEvents = recordings.stream().mapToInt(recording -> recording.getEvents().size()).sum();
        long totalRecordings = recordingService.countTotalRecordings(loggedUser);
        double totalDurationFormatted = recordings.stream().mapToDouble(Recording::getDuration).sum() ;
        double averageDurationFormatted = recordings.stream().mapToDouble(Recording::getDuration).average().orElse(0.0);
        long totalUnprocessedRecordings = recordingService.countTotalUnprocessedRecordings(loggedUser);
        long totalProcessedRecordings = recordingService.countTotalProcessedRecordings(loggedUser);
        long totalFraudEvents = recordings.stream().mapToLong(recording -> recording.getFraudEvents().size()).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("totalFraudPercentage", totalFraudPercentage);
        response.put("totalEvents", totalEvents);
        response.put("totalRecordings", totalRecordings);
        response.put("totalDuration", formatDuration(totalDurationFormatted));
        response.put("averageDuration", formatDuration(averageDurationFormatted));
        response.put("totalProcessedRecordings", totalProcessedRecordings);
        response.put("totalUnprocessedRecordings", totalUnprocessedRecordings);
        response.put("totalFraudEvents", totalFraudEvents);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recordingId}")
    public String recordingDetails(@PathVariable ObjectId recordingId, Model model) {
        Recording recording = recordingService.getRecordingById(recordingId, userService.getLoggedUser())
                .orElseThrow(() -> new RecordingNotFoundException("Recording not found with id: " + recordingId));

        // Procesar los datos para el gráfico de pastel
        Map<String, Integer> pieChartDataMap = new HashMap<>();
        Map<String, Integer> radarChartDataMap = new HashMap<>();

        for (Event event : recording.getEvents()) {
            switch (event.getEventType()) {
                case HEAD_POSE_DOWN:
                case HEAD_POSE_FORWARD:
                case HEAD_POSE_LEFT:
                case HEAD_POSE_RIGHT:
                case HEAD_POSE_UNKNOWN:
                case HEAD_POSE_UP:
                    radarChartDataMap.put(event.getEventType().toString(), radarChartDataMap.getOrDefault(event.getEventType().toString(), 0) + 1);
                    break;
                default:
                    if (recording.getEventsToDetect().contains(event.getEventType())) {
                        if (!pieChartDataMap.containsKey(event.getEventType().toString())){
                            pieChartDataMap.put(event.getEventType().toString(), 1);
                        }
                        else {
                            pieChartDataMap.put(event.getEventType().toString(), pieChartDataMap.get(event.getEventType().toString()) + 1);
                        }
                    }
                    break;
            }
        }

        // Convertir los mapas a listas para pasar al modelo
        List<String> pieChartLabels = new ArrayList<>(pieChartDataMap.keySet());
        List<Integer> pieChartData = new ArrayList<>(pieChartDataMap.values());

        List<String> radarChartLabels = new ArrayList<>(radarChartDataMap.keySet());
        List<Integer> radarChartData = new ArrayList<>(radarChartDataMap.values());

        // Agregar los atributos al modelo
        model.addAttribute("recording", recording);
        model.addAttribute("timelines", recording.getTimelines());
        model.addAttribute("pieChartLabels", pieChartLabels);
        model.addAttribute("pieChartData", pieChartData);
        model.addAttribute("radarChartLabels", radarChartLabels);
        model.addAttribute("radarChartData", radarChartData);

        return "recording/details";
    }

    @GetMapping("/api/recordings")
    @ResponseBody
    public DataTableResponse<Recording> getRecordingsByFolderId(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam(value = "search[value]", required = false) String searchValue,
            @RequestParam(value = "order[0][column]", required = false) int orderColumn,
            @RequestParam(value = "order[0][dir]", required = false) String orderDirection) {

        try {
            Pageable pageable = PageRequest.of(start / length, length);
            Page<Recording> RecordingsPage = recordingService.getAllRecordingsPageable(pageable, userService.getLoggedUser());

            List<Recording> recordings = recordingService.searchValueAndSorting(RecordingsPage.getContent(), searchValue, orderColumn, orderDirection);

            System.out.println(recordings);

            return new DataTableResponse<>(
                    draw,
                    RecordingsPage.getTotalElements(),
                    RecordingsPage.getTotalElements(),
                    recordings
            );
        } catch (Exception e) {
            return new DataTableResponse<>(draw, 0, 0, Collections.emptyList());
        }
    }

    @GetMapping("/api/recordings/{folderId}")
    @ResponseBody
    public DataTableResponse<Recording> getRecordingsByFolderId(
            @PathVariable String folderId,
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam(value = "search[value]", required = false) String searchValue,
            @RequestParam(value = "order[0][column]", required = false) int orderColumn,
            @RequestParam(value = "order[0][dir]", required = false) String orderDirection) {

        System.out.println("DRAW: " + draw);
        System.out.println("START: " + start);
        System.out.println("LENGHT: " + length);

        try {
            ObjectId objectId = new ObjectId(folderId);
            Pageable pageable = PageRequest.of(start / length, length);
            Page<Recording> RecordingsPage = recordingService.getAllRecordingsByFolderPageable(pageable, objectId, userService.getLoggedUser());

            List<Recording> recordings = recordingService.searchValueAndSorting(RecordingsPage.getContent(), searchValue, orderColumn, orderDirection);

            return new DataTableResponse<>(
                    draw,
                    RecordingsPage.getTotalElements(),
                    RecordingsPage.getTotalElements(),
                    recordings
            );
        } catch (Exception e) {
            return new DataTableResponse<>(draw, 0, 0, Collections.emptyList());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Recording> uploadRecordings(
            @RequestParam("folderId") String folderId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam("seconds") Double seconds) {

        // Validar el folderId si es necesario
        Folder folder = folderService.getFolderById(new ObjectId(folderId), userService.getLoggedUser())
                .orElseThrow(() -> new IllegalArgumentException("Invalid folder Id:" + folderId));

        FileDTO fileDTO;
        try {
            fileDTO = new FileDTO(file, Base64.getEncoder().encodeToString(thumbnail.getBytes()), seconds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Recording recording = recordingService.uploadFile(fileDTO, folder, userService.getLoggedUser());

        return ResponseEntity.ok(recording);
    }

    @PostMapping("/process")
    public ResponseEntity<?> processRecordings(@Valid @RequestBody RecordingProcessDTO recordingProcessDTO, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        System.out.println("Processing recordings");

        System.out.println("Recording IDs: " + recordingProcessDTO.getRecordingIds());
        System.out.println("Events to detect: " + recordingProcessDTO.getEventsToDetect());

        List<Recording> processedRecordings = recordingService.processRecordings(
                recordingProcessDTO.getRecordingIds(),
                recordingProcessDTO.getEventsToDetect(),
                userService.getLoggedUser());

        return ResponseEntity.ok(processedRecordings);
    }

    @PostMapping("/process-all")
    public ResponseEntity<?> processAllRecordings(@Valid @RequestBody RecordingProcessDTO recordingProcessDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        System.out.println("Events to detect: " + recordingProcessDTO.getEventsToDetect());

        List<Recording> processedRecordings = recordingService.processRecordings(
                recordingProcessDTO.getRecordingIds(),
                recordingProcessDTO.getEventsToDetect(),
                userService.getLoggedUser());

        return ResponseEntity.ok(processedRecordings);
    }

    @PostMapping("/process-all-without-process")
    public ResponseEntity<?> processAllWithoutProcess(@Valid @RequestBody RecordingProcessDTO recordingProcessDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        System.out.println("Events to detect: " + recordingProcessDTO.getEventsToDetect());

        List<Recording> processedRecordings = recordingService.processRecordings(
                recordingProcessDTO.getRecordingIds(),
                recordingProcessDTO.getEventsToDetect(),
                userService.getLoggedUser());

        return ResponseEntity.ok(processedRecordings);
    }

    @GetMapping("/image")
    public ResponseEntity<String> getImage(@RequestParam String fileId) {
        try {
            System.out.println("File ID: " + fileId);
            String base64Image = gridFsService.getImageAsBase64(fileId);
            return ResponseEntity.ok(base64Image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving image: " + e.getMessage());
        }
    }

    @GetMapping("{recordingId}/timelines/{timelineId}/segments/{segmentId}/images")
    public ResponseEntity<?> getSegmentImages(
            @PathVariable ObjectId recordingId,
            @PathVariable ObjectId timelineId,
            @PathVariable ObjectId segmentId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        System.out.println("Recording ID: " + recordingId);
        System.out.println("Timeline ID: " + timelineId);
        System.out.println("Segment ID: " + segmentId);
        System.out.println("Page: " + page);
        System.out.println("Size: " + size);

        List<String> imageIds;
        long totalImages;

        if (page == null || size == null || size <= 0) {
            System.out.println("Getting all images");
            // Obtener todas las imágenes
            imageIds = recordingService.getImagesForSegment(timelineId, recordingId, segmentId);

            System.out.println("Total images: " + imageIds.size());

            // Convertir las imágenes a base64 con el servicio de GridFS
            List<String> imageBase64List = new ArrayList<>();
            for (String imageId : imageIds) {
                String base64Image = gridFsService.getImageAsBase64(imageId);
                imageBase64List.add(base64Image);
            }

            System.out.println("Total images: " + imageBase64List.size());

            return ResponseEntity.ok(imageBase64List);
        }

        // Obtener imágenes paginadas
        imageIds = recordingService.getImagesForSegment(timelineId, recordingId, segmentId, page, size);
        totalImages = recordingService.getTotalImagesForSegment(timelineId, recordingId, segmentId);

        // Convertir las imágenes a base64 con el servicio de GridFS
        List<String> imageBase64List = new ArrayList<>();
        for (String imageId : imageIds) {
            String base64Image = gridFsService.getImageAsBase64(imageId);
            imageBase64List.add(base64Image);
        }

        // Crear un objeto Page para la paginación
        Page<String> imagePage = new PageImpl<>(imageBase64List, PageRequest.of(page, size), totalImages);
        return ResponseEntity.ok(imagePage);
    }

    @GetMapping("{recordingId}/timelines/{timelineId}/segments/{segmentId}/gif")
    public ResponseEntity<String> makeGif(
            @PathVariable ObjectId recordingId,
            @PathVariable ObjectId timelineId,
            @PathVariable ObjectId segmentId) throws IOException {

        System.out.println("Recording ID: " + recordingId);
        System.out.println("Timeline ID: " + timelineId);
        System.out.println("Segment ID: " + segmentId);

        List<String> imageIds = recordingService.getImagesForSegment(timelineId, recordingId, segmentId);

        System.out.println("Imagenes obtenidas");

        // Convertir las imágenes a base64 con el servicio de GridFS
        List<byte[]> imageBytesList = new ArrayList<>();
        for (String imageId : imageIds) {
            byte[] imageBytes = gridFsService.getImageAsBytes(imageId);
            imageBytesList.add(imageBytes);
        }

        System.out.println("Imagenes convertidas");

        // Convertir las imágenes a un gif
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
        gifEncoder.setDelay(200);
        gifEncoder.setRepeat(0);
        gifEncoder.setQuality(10); // Calidad ajustada para mejor rendimiento

        gifEncoder.start(outputStream);

        System.out.println("Gif encoder iniciado");

        for (byte[] imageBytes : imageBytesList) {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            gifEncoder.addFrame(bufferedImage);
        }

        System.out.println("Imagenes agregadas al gif");

        gifEncoder.finish();

        System.out.println("Gif finalizado");

        byte[] gifData = outputStream.toByteArray();
        String base64Gif = Base64.getEncoder().encodeToString(gifData);

        String base64GifUrl = "data:image/gif;base64," + base64Gif;

        System.out.println("Gif convertido a base64");

        return ResponseEntity.ok(base64GifUrl);
    }

    @DeleteMapping("/delete/{recordingId}")
    public ResponseEntity<?> deleteRecording(@PathVariable ObjectId recordingId) {
        System.out.println("Deleting recording with ID: " + recordingId);
        try {
            recordingService.deleteRecording(recordingId, userService.getLoggedUser());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting recording: " + e.getMessage());
        }
    }


    private String formatDuration(double totalSeconds) {
        long hours = (long) totalSeconds / 3600;
        long minutes = ((long) totalSeconds % 3600) / 60;
        long seconds = (long) totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
