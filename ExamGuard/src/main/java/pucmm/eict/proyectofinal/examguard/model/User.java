package pucmm.eict.proyectofinal.examguard.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pucmm.eict.proyectofinal.examguard.model.enums.UserRole;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdDeserializer;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdSerializer;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String name;
    private String role;
    private String institution;
    private String email;
    private String password;
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}