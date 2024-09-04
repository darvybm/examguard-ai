package pucmm.eict.proyectofinal.examguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pucmm.eict.proyectofinal.examguard.model.enums.EventType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @JsonProperty("second")
    private int second;

    @JsonProperty("event_type")
    private EventType eventType;

    @JsonProperty("image")
    private String image;

    @Override
    public String toString() {
        return "Event{" +
                "second=" + second +
                ", type=" + eventType +
                '}';
    }

}