package pucmm.eict.proyectofinal.examguard.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.BadRequestException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pucmm.eict.proyectofinal.examguard.dto.RecordingDTO;
import pucmm.eict.proyectofinal.examguard.model.Folder;
import pucmm.eict.proyectofinal.examguard.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Component
public class DataBucketUtil {

    public static final String GCP_CONFIG_FILE = "gcp-account-file.json";
    public static final String GCP_PROJECT_ID = "sonorous-guide-407801";
    public static final String GCP_BUCKET_ID = "exam_guard";

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBucketUtil.class);

    public RecordingDTO uploadFile(MultipartFile multipartFile, String fileName, String contentType, User user, Folder folder) throws Exception {

        try{
            LOGGER.debug("Start file uploading process on GCS");
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

            InputStream inputStream = new ClassPathResource(GCP_CONFIG_FILE).getInputStream();

            StorageOptions options = StorageOptions.newBuilder().setProjectId(GCP_PROJECT_ID)
                    .setCredentials(GoogleCredentials.fromStream(inputStream)).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(GCP_BUCKET_ID,Storage.BucketGetOption.fields());

            ObjectId objectId = new ObjectId();

            Blob blob = bucket.create("user-" + user.getId() + "/"
                    + "folder-" + folder.getId() + "/"
                    + "recording-" + objectId + "/"
                    + fileName + "-" + objectId + checkFileExtension(fileName),
                    fileData, contentType);

            if(blob != null){
                LOGGER.debug("File successfully uploaded to GCS");
                return new RecordingDTO(objectId, fileName, blob.getName());
            }

        }catch (Exception e){
            LOGGER.error("An error occurred while uploading data. Exception: ", e);
            throw new Exception("An error occurred while storing data to GCS");
        }
        throw new Exception("An error occurred while storing data to GCS");
    }

    private File convertFile(MultipartFile file) throws Exception {

        try{
            if(file.getOriginalFilename() == null){
                throw new BadRequestException("Original file name is null");
            }
            File convertedFile = new File(file.getOriginalFilename());
            FileOutputStream outputStream = new FileOutputStream(convertedFile);
            outputStream.write(file.getBytes());
            outputStream.close();
            LOGGER.debug("Converting multipart file : {}", convertedFile);
            return convertedFile;
        }catch (Exception e){
            throw new Exception("An error has occurred while converting the file");
        }
    }

    private String checkFileExtension(String fileName) throws Exception {
        if(fileName != null && fileName.contains(".")){
            String[] extensionList = {".png", ".jpeg", ".pdf", ".doc", ".mp3", ".mp4"};

            for(String extension: extensionList) {
                if (fileName.endsWith(extension)) {
                    LOGGER.debug("Accepted file type : {}", extension);
                    return extension;
                }
            }
        }
        LOGGER.error("Not a permitted file type");
        throw new Exception("Not a permitted file type");
    }
}
