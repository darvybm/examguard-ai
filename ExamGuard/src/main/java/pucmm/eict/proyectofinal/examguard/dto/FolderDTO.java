package pucmm.eict.proyectofinal.examguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderDTO {

    @NotBlank(message = "El nombre del folder no puede estar vacío")
    @Size(max = 25, message = "El nombre del folder no puede exceder los 25 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción del folder no puede exceder los 1000 caracteres")
    private String description;
}
