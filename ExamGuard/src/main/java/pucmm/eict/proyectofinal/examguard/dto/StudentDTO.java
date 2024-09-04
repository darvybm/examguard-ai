package pucmm.eict.proyectofinal.examguard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    @NotBlank(message = "El nombre del estudiante no puede estar vacío.")
    @Size(max = 100, message = "El nombre del estudiante no puede exceder los 100 caracteres.")
    private String name;

    @NotBlank(message = "El correo electrónico del estudiante no puede estar vacío.")
    @Email(message = "El correo electrónico debe ser válido.")
    private String email;
}
