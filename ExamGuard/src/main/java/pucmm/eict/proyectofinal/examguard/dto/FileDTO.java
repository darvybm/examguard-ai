package pucmm.eict.proyectofinal.examguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {
    MultipartFile file;
    String thumbnail;
    Double seconds;
}
