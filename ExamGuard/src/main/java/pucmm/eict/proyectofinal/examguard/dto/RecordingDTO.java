package pucmm.eict.proyectofinal.examguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingDTO {
    ObjectId objectId;
    String name;
    String url;
}
