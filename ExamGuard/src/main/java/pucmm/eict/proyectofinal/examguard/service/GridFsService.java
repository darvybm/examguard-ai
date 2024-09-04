package pucmm.eict.proyectofinal.examguard.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GridFsService {

    private final GridFsTemplate template;
    private final GridFsOperations operations;

    public String uploadImageToGridFs(String base64Image) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        InputStream imageStream = new ByteArrayInputStream(imageBytes);
        String filename = UUID.randomUUID() + ".jpg"; // You may want to adjust the file extension based on the image type

        // Save the image to GridFS
        return template.store(imageStream, filename).toString();
    }

    public String getImageAsBase64(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        GridFSFile gridFSFile = template.findOne(query);

        if (gridFSFile == null) {
            throw new RuntimeException("File not found with id: " + id);
        }

        try {
            byte[] imageBytes = IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream());
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file from GridFS: " + e.getMessage(), e);
        }
    }

    // Método para eliminar una imagen de GridFS
    public void deleteImageFromGridFs(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        GridFSFile gridFSFile = template.findOne(query);

        if (gridFSFile == null) {
            throw new RuntimeException("File not found with id: " + id);
        }

        template.delete(query);
    }

    public byte[] getImageAsBytes(String imageId) {
        Query query = new Query(Criteria.where("_id").is(imageId));
        GridFSFile gridFSFile = template.findOne(query);

        if (gridFSFile == null) {
            throw new RuntimeException("File not found with id: " + imageId);
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(operations.getResource(gridFSFile).getInputStream(), outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error reading file from GridFS: " + e.getMessage(), e);
        }
    }
}
