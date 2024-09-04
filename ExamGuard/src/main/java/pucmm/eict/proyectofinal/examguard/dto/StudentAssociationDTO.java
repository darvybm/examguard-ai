package pucmm.eict.proyectofinal.examguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAssociationDTO {
    private ObjectId recordingId;
    private ObjectId studentId;
}
