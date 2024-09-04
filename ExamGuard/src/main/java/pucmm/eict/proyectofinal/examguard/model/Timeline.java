package pucmm.eict.proyectofinal.examguard.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import pucmm.eict.proyectofinal.examguard.model.enums.EventType;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdDeserializer;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdSerializer;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Timeline {
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private EventType eventType;
    private List<TimelineSegment> segments = new ArrayList<>();

    public int getFraudDuration() {
        return segments.stream().mapToInt(TimelineSegment::getDuration).sum();
    }
}
