package pucmm.eict.proyectofinal.examguard.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdDeserializer;
import pucmm.eict.proyectofinal.examguard.utils.ObjectIdSerializer;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "folders")
public class Folder {

    @Id
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    private String name;
    private String description;

    @DBRef
    private User user;

    @DBRef
    @JsonBackReference
    private List<Recording> recordings = new ArrayList<>();

    public double getAverageDuration() {
        try {
            if (recordings.isEmpty()) {
                return 0.0;
            }
            double totalDuration = 0;
            for (Recording recording : recordings) {
                totalDuration += recording.getDuration();
            }
            return totalDuration / recordings.size();
        } catch (Exception e) {
            // Logging the error might be a good idea
            System.err.println("Error calculating average duration: " + e.getMessage());
            return 0.0; // Default value in case of error
        }
    }

    public String getFormattedAverageDuration() {
        try {
            if (recordings.isEmpty()) {
                return "00:00:00";
            }
            double totalDuration = 0;
            for (Recording recording : recordings) {
                totalDuration += recording.getDuration();
            }
            double averageDuration = totalDuration / recordings.size();

            int hours = (int) (averageDuration / 3600);
            int minutes = (int) ((averageDuration % 3600) / 60);
            int seconds = (int) (averageDuration % 60);

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            // Logging the error might be a good idea
            System.err.println("Error calculating average duration: " + e.getMessage());
            return "00:00:00"; // Default value in case of error
        }
    }


    public double getAverageFraud() {
        try {
            if (recordings.isEmpty()) {
                return 0.0;
            }
            double totalFraud = 0;
            int processedCount = 0;
            for (Recording recording : recordings) {
                if (recording.isProcessed()) { // Asegúrate de tener un método isProcessed en tu clase Recording
                    totalFraud += recording.getFraudPercentage();
                    processedCount++;
                }
            }
            if (processedCount == 0) {
                return 0.0; // Si no hay ningún recording procesado, devolver 0
            }
            return totalFraud / processedCount;
        } catch (Exception e) {
            // Logging the error might be a good idea
            System.err.println("Error calculating average fraud percentage: " + e.getMessage());
            return 0.0; // Default value in case of error
        }
    }

    public int getTotalEvents() {
        int suma = 0;
        try {
            for (Recording recording : recordings) {
                suma += recording.getFraudEvents().size();
            }
        } catch (NullPointerException | UnsupportedOperationException e) {
            System.err.println("Error al calcular el total de eventos: " + e.getMessage());
            return 0;
        }
        return suma;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user=" + (user != null ? user.getEmail() : "null") + // Ejemplo de acceso seguro a user
                ", recordings=" + getRecordingIds() + // Mostrar solo los IDs de las grabaciones
                '}';
    }

    private List<String> getRecordingIds() {
        List<String> ids = new ArrayList<>();
        for (Recording recording : recordings) {
            ids.add(recording.getId().toString());
        }
        return ids;
    }
}
