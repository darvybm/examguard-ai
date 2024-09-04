package pucmm.eict.proyectofinal.examguard.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pucmm.eict.proyectofinal.examguard.model.enums.EventType;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdDeserializer;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "recordings")
public class Recording {

    @Id
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String name;
    private String url;
    private String thumbnail;
    @DBRef
    private User user;
    @DBRef
    private Folder folder;
    @DBRef
    private Student student;
    private List<Event> events = new ArrayList<>();
    private List<EventType> eventsToDetect = new ArrayList<>();
    private Double duration;
    private List<Timeline> timelines = new ArrayList<>();
    private boolean processed = false;
    private Double fraudPercentage;

    public String getFormattedDuration() {
        try {
            Double totalSeconds = getDuration();
            int hours = (int) (totalSeconds / 3600);
            int minutes = (int) ((totalSeconds % 3600) / 60);
            int seconds = (int) (totalSeconds % 60);
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            // Logging the error might be a good idea
            System.err.println("Error formatting duration: " + e.getMessage());
            return "00:00:00"; // Default value in case of error
        }
    }

    public List<Event> getFraudEvents() {
        try {
            return events.stream()
                    .filter(event -> eventsToDetect.contains(event.getEventType()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Logging the error might be a good idea
            System.err.println("Error getting fraud events: " + e.getMessage());
            return new ArrayList<>(); // Default value in case of error
        }
    }
}
