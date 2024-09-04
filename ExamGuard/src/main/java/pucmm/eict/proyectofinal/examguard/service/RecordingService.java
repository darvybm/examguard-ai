package pucmm.eict.proyectofinal.examguard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pucmm.eict.proyectofinal.examguard.dto.FileDTO;
import pucmm.eict.proyectofinal.examguard.exception.FolderAccessDeniedException;
import pucmm.eict.proyectofinal.examguard.exception.RecordingAccessDeniedException;
import pucmm.eict.proyectofinal.examguard.exception.RecordingNotFoundException;
import pucmm.eict.proyectofinal.examguard.exception.StudentNotFoundException;
import pucmm.eict.proyectofinal.examguard.model.*;
import pucmm.eict.proyectofinal.examguard.model.enums.EventType;
import pucmm.eict.proyectofinal.examguard.repository.FolderRepository;
import pucmm.eict.proyectofinal.examguard.repository.RecordingRepository;
import pucmm.eict.proyectofinal.examguard.repository.StudentRepository;
import pucmm.eict.proyectofinal.examguard.utils.TimelineGenerator;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final RecordingRepository recordingRepository;
    private final FolderRepository folderRepository;
    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;
    private final GridFsService gridFsService;
    private final JwtTokenService jwtTokenService;

    @Value("${flask.api.url}")
    private String API_URL;

    // Check if the user has access to the recording
    private void checkUserAccess(Recording recording, User loggedUser) {
        if (!recording.getUser().equals(loggedUser)) {
            throw new RecordingAccessDeniedException("Access denied: User does not own this recording.");

        }
    }

    // Check if the user has access to the folder
    private void checkUserAccess(Folder folder, User loggedUser) {
        if (!folder.getUser().equals(loggedUser)) {
            throw new FolderAccessDeniedException("Access denied: User does not own this folder.");
        }
    }

    public List<Recording> getRecordingsByUser(User loggedUser) {
        return recordingRepository.findAllByUser(loggedUser);
    }

    public Optional<Recording> getRecordingById(ObjectId recordingId, User loggedUser) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found with id: " + recordingId));
        checkUserAccess(recording, loggedUser);
        return Optional.of(recording);
    }

    public Recording uploadFile(FileDTO fileDTO, Folder folder, User loggedUser) {
        checkUserAccess(folder, loggedUser);
        try {
            Recording recording = createRecording(fileDTO, folder, folder.getUser());
            String url = API_URL + "/store";

            MultipartFile multipartFile = fileDTO.getFile();

            // Configurar los datos para el envío
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("userId", recording.getUser().getId().toString());
            body.add("folderId", folder.getId().toString());
            body.add("recordingId", recording.getId().toString());
            body.add("file", multipartFile.getResource());

            // Generar el token JWT
            String token = jwtTokenService.generateToken();

            // Configurar los encabezados HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Enviar solicitud POST
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                recording = recordingRepository.save(recording);
                folder.getRecordings().add(recording);

                // Actualizar la carpeta en la base de datos
                Query query = new Query(Criteria.where("_id").is(folder.getId()));
                Update update = new Update().push("recordings", recording.getId());
                mongoTemplate.updateFirst(query, update, Folder.class);

                return recording;
            } else {
                throw new RuntimeException("Failed to upload file to Flask API: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error uploading and saving recording: " + e.getMessage(), e);
        }
    }


    public List<Recording> processRecordings(List<String> recordingIds, List<EventType> eventsToDetect, User loggedUser) {
        List<Recording> processedRecordings = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("Recording Ids: " + recordingIds);

        for (String recordingId : recordingIds) {
            Recording recording = recordingRepository.findById(new ObjectId(recordingId))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid recording Id:" + recordingId));
            checkUserAccess(recording, loggedUser);
            String url = API_URL + "/process";

            if (recording.isProcessed()) {
                recording.setEventsToDetect(eventsToDetect);
                recording.setTimelines(TimelineGenerator.generateTimeline(recording));
                double fraudPercentage = calculateFraudPercentage(recording.getTimelines(), eventsToDetect, recording.getDuration());
                recording.setFraudPercentage(fraudPercentage);

                System.out.println("Recording already processed: " + recording.getName());
                System.out.println("Fraud percentage: " + fraudPercentage);

                recordingRepository.save(recording);
                processedRecordings.add(recording);
                continue;
            }

            try {
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("recordingId", recordingId);
                requestData.put("url", recording.getUrl());
                requestData.put("userId", recording.getUser().getId().toString());
                requestData.put("folderId", recording.getFolder().getId().toString());

                String token = jwtTokenService.generateToken();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + token);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

                String processedRecordingJson = restTemplate.postForObject(url, requestEntity, String.class);
                Map<String, List<Event>> responseMap = objectMapper.readValue(processedRecordingJson, new TypeReference<>() {});
                List<Event> events = responseMap.get("processed_recording");

                for (Event event : events) {
                    if (event.getImage() != null) {
                        String imageId = gridFsService.uploadImageToGridFs(event.getImage());
                        event.setImage(imageId);
                    }
                }

                recording.setEvents(events);
                recording.setEventsToDetect(eventsToDetect);
                recording.setProcessed(true);
                recording.setTimelines(TimelineGenerator.generateTimeline(recording));

                double fraudPercentage = calculateFraudPercentage(recording.getTimelines(), eventsToDetect, recording.getDuration());
                recording.setFraudPercentage(fraudPercentage);

                recordingRepository.save(recording);
                processedRecordings.add(recording);

            } catch (Exception e) {
                throw new RuntimeException("Error processing recording: " + e.getMessage(), e);
            }
        }
        return processedRecordings;
    }

    private double calculateFraudPercentage(List<Timeline> timelines, List<EventType> eventsToDetect, Double totalDuration) {
        // Lista para almacenar todos los segmentos de fraude
        List<TimelineSegment> allFraudSegments = new ArrayList<>();

        // Recolectar todos los segmentos de fraude en una sola lista
        for (Timeline timeline : timelines) {
            if (eventsToDetect.contains(timeline.getEventType())) {
                allFraudSegments.addAll(timeline.getSegments());
            }
        }

        // Fusionar los intervalos de fraude solapados
        List<Interval> mergedFraudIntervals = mergeIntervals(allFraudSegments);

        // Calcular el tiempo total de fraude basado en los intervalos fusionados
        double totalFraudTime = 0;
        for (Interval interval : mergedFraudIntervals) {
            totalFraudTime += interval.getDuration();
        }

        // Calcular el porcentaje de fraude global
        double fraudPercentage = (totalDuration > 0) ? (totalFraudTime / totalDuration) * 100 : 0;

        return Math.min(Math.max(fraudPercentage, 0), 100); // Asegúrate de que el porcentaje esté entre 0 y 100
    }

    // Método para fusionar intervalos solapados
    private List<Interval> mergeIntervals(List<TimelineSegment> segments) {
        // Convertir los segmentos en intervalos
        List<Interval> intervals = segments.stream()
                .map(segment -> new Interval(segment.getStartSecond(), segment.getEndSecond()))
                .sorted(Comparator.comparingInt(Interval::getStart))
                .toList();

        List<Interval> mergedIntervals = new ArrayList<>();
        for (Interval interval : intervals) {
            if (mergedIntervals.isEmpty()) {
                mergedIntervals.add(interval);
            } else {
                Interval last = mergedIntervals.get(mergedIntervals.size() - 1);
                if (interval.getStart() <= last.getEnd()) {
                    last.setEnd(Math.max(last.getEnd(), interval.getEnd()));
                } else {
                    mergedIntervals.add(interval);
                }
            }
        }
        return mergedIntervals;
    }

    private Recording createRecording(FileDTO fileDTO, Folder folder, User loggedUser) {

        checkUserAccess(folder, loggedUser);

        ObjectId recordingId = new ObjectId();
        Recording recording = new Recording();
        recording.setId(recordingId);
        recording.setName(fileDTO.getFile().getOriginalFilename());

        // Definir nombres descriptivos para las carpetas
        String userFolder = "user_" + loggedUser.getId();
        String folderFolder = "folder_" + folder.getId();
        String recordingFolder = "recording_" + recordingId;

        // Construir la URL con el formato deseado
        String fileExtension = getFileExtension(fileDTO.getFile());
        String url = String.format("recordings/%s/%s/%s/%s%s",
                userFolder, folderFolder, recordingFolder, recordingId, fileExtension);

        recording.setUrl(url);
        recording.setEvents(new ArrayList<>());
        recording.setUser(loggedUser);
        recording.setFolder(folder);
        recording.setDuration(fileDTO.getSeconds());
        recording.setThumbnail(fileDTO.getThumbnail());
        recording.setProcessed(false);
        recording.setTimelines(new ArrayList<>());
        recording.setFraudPercentage(0.0);

        return recording;
    }

    @Transactional
    public void deleteRecording(ObjectId recordingId, User loggedUser) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found with id: " + recordingId));

        checkUserAccess(recording, loggedUser);

        try {
            // Eliminar imágenes relacionadas con el recording
            for (Event event : recording.getEvents()) {
                if (event.getImage() != null) {
                    System.out.println("Deleting image: " + event.getImage());
                    gridFsService.deleteImageFromGridFs(event.getImage());
                }
            }

            System.out.println("Deleting recording: " + recording.getName());

            // Eliminar el archivo de la grabación en el sistema de ficheros en python
            String url = API_URL + "/delete";
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", recording.getUser().getId().toString());
            requestData.put("recordingId", recording.getId().toString());
            requestData.put("recordingUrl", recording.getUrl());

            String token = jwtTokenService.generateToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            System.out.println("Request Entity: " + requestEntity);

            restTemplate.postForObject(url, requestEntity, String.class);

        }catch (Exception e) {
            throw new RuntimeException("Error deleting recording: " + e.getMessage(), e);
        }

        Folder folder = recording.getFolder();
        folder.getRecordings().remove(recording);

        folderRepository.save(folder);
        recordingRepository.delete(recording);
    }

    public List<String> getImagesForSegment(ObjectId timelineId, ObjectId recordingId, ObjectId segmentId, int page, int size) {
        System.out.println("recordingId: " + recordingId);
        System.out.println("timelineId: " + timelineId);
        System.out.println("segmentId: " + segmentId);
        System.out.println("Page: " + page);
        System.out.println("Size: " + size);

        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found with id: " + recordingId));


        // Obtener el timeline
        Timeline timeline = recording.getTimelines().stream()
                .filter(t -> t.getId().equals(timelineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Timeline not found with id: " + timelineId));

        // Obtener el segmento
        TimelineSegment segment = timeline.getSegments().stream()
                .filter(s -> s.getId().equals(segmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Segment not found with id: " + segmentId));

        // Aplicar paginación
        List<String> imageIds = segment.getImages();
        int start = Math.min(page * size, imageIds.size());
        int end = Math.min((page + 1) * size, imageIds.size());
        return imageIds.subList(start, end);
    }


    public String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        } else {
            return "";
        }
    }


    public Page<Recording> getAllRecordingsPageable(Pageable pageable, User loggedUser) {
        return recordingRepository.findAllByUser(loggedUser, pageable);
    }

    public Page<Recording> getAllRecordingsByFolderPageable(Pageable pageable, ObjectId folderId, User loggedUser) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found with id: " + folderId));
        checkUserAccess(folder, loggedUser);
        return recordingRepository.findAllByFolderId(folderId, pageable);
    }

    public List<Recording> searchValueAndSorting(List<Recording> t, String searchValue, int orderColumn, String orderDirection) {

        return t.stream()
                .filter(containsText(normalize(searchValue.toLowerCase())))
                .toList()
                .stream()
                .sorted(buildSorter(orderColumn, orderDirection))
                .toList();
    }

    private Comparator<Recording> buildSorter(int orderColumn, String orderDirection) {
        List<Comparator<Recording>> sortChain = new ArrayList<>();
        sortChain.add(buildComparatorClause(orderColumn, orderDirection));
        return (Recording1, Recording2) -> {
            for (Comparator<Recording> comparator : sortChain) {
                int result = comparator.compare(Recording1, Recording2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }

    private Comparator<Recording> buildComparatorClause(int orderColumn, String orderDirection) {
        // Map column indices to comparators for conciseness
        Map<Integer, Comparator<Recording>> comparators = Map.of(
                1, Comparator.comparing(Recording::getName, String.CASE_INSENSITIVE_ORDER),
                2, Comparator.comparing(Recording::getDuration),
                3, Comparator.comparing(Recording::isProcessed),
                4, Comparator.comparing(t -> t.getFraudEvents().size()),
                5, Comparator.comparing(Recording::getFraudPercentage));

        Comparator<Recording> comp = comparators.getOrDefault(orderColumn, Comparator.comparing(Recording::getId));

        return "asc".equals(orderDirection) ? comp : comp.reversed();
    }

    public Predicate<Recording> containsText(String searchTerm) {
        return recording -> {
            User user = recording.getUser();
            Folder folder = recording.getFolder();

            return (recording.getId() != null && normalize(recording.getId().toString().toLowerCase()).contains(searchTerm))
                    || (recording.getName() != null && normalize(recording.getName().toLowerCase()).contains(searchTerm))
                    || (recording.getUrl() != null && normalize(recording.getUrl().toLowerCase()).contains(searchTerm))
                    || (user != null && normalize(user.getName().toLowerCase()).contains(searchTerm))
                    || (folder != null && normalize(folder.getName().toLowerCase()).contains(searchTerm))
                    || (recording.getEvents().stream().anyMatch(event -> eventContainsText(event, searchTerm)))
                    || (recording.isProcessed() && normalize("processed").contains(searchTerm));
        };
    }

    private boolean eventContainsText(Event event, String searchTerm) {
        // Implement logic to check if event contains search term
        return event != null && normalize(event.toString().toLowerCase()).contains(searchTerm);
    }

    private String normalize(String input) {
        if (input == null) {
            return null;
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{Mn}", "");
    }

    public long getTotalImagesForSegment(ObjectId timelineId, ObjectId recordingId, ObjectId segmentId) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found with id: " + recordingId));

        // Obtener el timeline
        Timeline timeline = recording.getTimelines().stream()
                .filter(t -> t.getId().equals(timelineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Timeline not found with id: " + timelineId));

        // Obtener el segmento
        TimelineSegment segment = timeline.getSegments().stream()
                .filter(s -> s.getId().equals(segmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Segment not found with id: " + segmentId));

        return segment.getImages().size();
    }

    public long countRecordingsByUser(User loggedUser) {
        return recordingRepository.countByUser(loggedUser);
    }

    public long countProcessedRecordingsByUser(User loggedUser) {
        return recordingRepository.countByUserAndProcessed(loggedUser, true);
    }

    public long countTotalRecordings(User user) {
        return recordingRepository.countByUser(user);
    }

    public long countTotalUnprocessedRecordings(User user) {
        return recordingRepository.countByUserAndProcessed(user, false);
    }

    public long countTotalProcessedRecordings(User user) {
        return recordingRepository.countByUserAndProcessed(user, true);
    }

    public List<String> getImagesForSegment(ObjectId timelineId, ObjectId recordingId, ObjectId segmentId) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found with id: " + recordingId));


        // Obtener el timeline
        Timeline timeline = recording.getTimelines().stream()
                .filter(t -> t.getId().equals(timelineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Timeline not found with id: " + timelineId));

        // Obtener el segmento
        TimelineSegment segment = timeline.getSegments().stream()
                .filter(s -> s.getId().equals(segmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Segment not found with id: " + segmentId));

        return segment.getImages();
    }

    public void updateRecording(Recording recording, User loggedUser) {
        checkUserAccess(recording, loggedUser);
        recordingRepository.save(recording);
    }
}
