package pucmm.eict.proyectofinal.examguard.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pucmm.eict.proyectofinal.examguard.model.enums.EventType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingProcessDTO {

    @NotNull(message = "La lista de IDs de grabación no debe ser nula")
    @NotEmpty(message = "La lista de IDs de grabación no debe estar vacía")
    private List<String> recordingIds;

    @NotNull(message = "La lista de eventos a detectar no debe ser nula")
    @NotEmpty(message = "La lista de eventos a detectar no debe estar vacía")
    private List<EventType> eventsToDetect;
}
