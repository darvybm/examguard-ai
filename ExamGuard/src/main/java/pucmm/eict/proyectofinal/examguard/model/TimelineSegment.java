package pucmm.eict.proyectofinal.examguard.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdDeserializer;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelineSegment {
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private int startSecond;
    private int endSecond;
    private List<String> images = new ArrayList<>();

    public int getDuration() {
        int duration = endSecond - startSecond;
        return Math.max(duration, 1); // Si duration es 0, retornará 1
    }
}
